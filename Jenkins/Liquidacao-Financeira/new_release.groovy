def repository = 'ssh://git@stash.matera.com:7999/cbank/liquidacao-financeira.git'
def slackChannel = '#c6-liquidacao-financeira-jenkins'

properties(
    [
        disableConcurrentBuilds(),
        parameters(
            [
                string(defaultValue: '', description: 'Versão atual (opcional). Exemplo: 1.0.0-BUILD01', name: 'versaoAtual'),
                string(defaultValue: '', description: 'Próxima versão (opcional). Exemplo: 1.0.0-BUILD02', name: 'proximaVersao'),
                string(defaultValue: 'c6-v7.11.05.00.f013', description: 'Branch de onde será gerada a release', name: 'branch')
            ]
        ),
        pipelineTriggers([])
    ]
)

node('master') {
    
    if (params.branch == '') {
        error "Branch não informado"
    }
}

node('jdk8-maven-slim') {
    try {
        stage('Checkout') {
            git branch: "${params.branch}", credentialsId: '2a64b1d3-8c92-4ee8-8db0-b766ec6f9c84', url: repository
        }

        stage('Prepare') {

            def pom = readMavenPom()

            def currentVersion = params.versaoAtual.trim()
            def nextVersion = params.proximaVersao.trim()

            if (currentVersion == '') {
                currentVersion = pom.version.minus('-SNAPSHOT')
            }

            if (nextVersion == '') {
                splitted = currentVersion.tokenize('-.')
                last_string = splitted.last()
                last_string = last_string.minus('build')
                last = last_string as Integer
                start = currentVersion.getAt(0..-(last_string.length() + 1))
                if (last < 9) {
                    start += '0'
                }
                nextVersion = start + (last + 1)
            }

            env.MAIN_BRANCH = "${params.branch}"
            env.CURRENT_VERSION = "${currentVersion}"
            env.NEXT_VERSION = "${nextVersion}-SNAPSHOT"
            env.TAG = "${currentVersion}"

            wrap([$class: 'BuildUser']) {

                env.BUILD_USER = "${BUILD_USER}"
                env.BUILD_EMAIL = "${BUILD_USER_EMAIL}"

                sh 'git config --global user.email "${BUILD_USER_EMAIL}"'
                sh 'git config --global user.name "${BUILD_USER}"'

            }

        }

        stage('Tag') {

            sshagent(['2a64b1d3-8c92-4ee8-8db0-b766ec6f9c84']) {

                // Branch
                sh 'git checkout ${MAIN_BRANCH}'
                sh 'git pull origin ${MAIN_BRANCH}'

                // Versão do Release
                sh 'git checkout -b release/${TAG}'
                updateVersionWithMaven(env.CURRENT_VERSION)

                sh 'git commit -a -m "Releasing ${TAG} from ${MAIN_BRANCH} by ${BUILD_USER} through Jenkins (${BUILD_URL})"'

                sh 'git tag $TAG'
                sh 'git push origin $TAG'

            }

        }

        stage('Build Package') {

            withMaven(
                    mavenSettingsConfig: '3fa78956-16be-4f78-8c04-b8b7410811c4',
                    mavenLocalRepo: '.repository',
                    options: [
                            artifactsPublisher(disabled: true),
                            findbugsPublisher(disabled: true),
                            openTasksPublisher(disabled: true)
                    ]
            ) {
                withCredentials([file(credentialsId: 'MATERA_KEYSTORE', variable: 'KEYSTORE')]) {
                    env.MATERA_KEYSTORE = "${KEYSTORE}"
                    sh 'mvn clean install -Dwebstart.timestamp.url=http://time.certum.pl'
                }
            }
            archive "**/target/*.war"
            sh 'git reset --hard'
        }
        
        stage('Deploy api-client') {

            withMaven(
                    mavenSettingsConfig: '3fa78956-16be-4f78-8c04-b8b7410811c4',
                    mavenLocalRepo: '.repository',
                    options: [
                            artifactsPublisher(disabled: true),
                            findbugsPublisher(disabled: true),
                            openTasksPublisher(disabled: true)
                    ]
            ) {
                sh 'mvn clean deploy -f api-client/pom.xml'
            }
            sh 'git reset --hard'
        }        

        stage('Build Database') {
            def currentVersion = "${env.CURRENT_VERSION}"
            def versionDb = generateDbVersion(currentVersion)
            echo 'Building database from ' + currentVersion + ' to ' + versionDb

            
            configFileProvider([configFile(fileId: 'gradle-mirror', targetLocation: '/root/.gradle/init.gradle')]) {
                sh "./gradlew clean buildBackend -Pend=HEAD -Pversion=${versionDb} --refresh-dependencies --stacktrace -Dwrap.local=false"
            }
            archive "**/*.zip"

        }

        stage('Next Version') {

            sshagent(['2a64b1d3-8c92-4ee8-8db0-b766ec6f9c84']) {

                // Merge no branch
                sh 'git checkout ${MAIN_BRANCH}'
                sh 'git merge --no-ff -X theirs -m "Merging release/${TAG} into ${MAIN_BRANCH} using theirs strategy" release/${TAG}'

                // Próxima versão
                updateVersionWithMaven(env.NEXT_VERSION)
                sh 'git commit -a -m "Updating to next version: ${NEXT_VERSION}"'
                sh 'git push origin ${MAIN_BRANCH}'

            }

            currentBuild.description = "Versão ${env.CURRENT_VERSION}"
        }
    } catch (ignored) {
        currentBuild.result = 'FAILURE'
    } finally {
        sendNotificationsToSlack(slackChannel, currentBuild.currentResult)
    }
}

@NonCPS
def generateDbVersion(String version) {
    def pomVersionPattern = "^((?:\\d+\\.){2}\\d+).*"
    if(CURRENT_VERSION.matches(pomVersionPattern)) {
        return CURRENT_VERSION.replaceAll(pomVersionPattern, '\$1') 
    }
}

def updateVersionWithMaven(String version) {
    withMaven(
                    mavenSettingsConfig: '3fa78956-16be-4f78-8c04-b8b7410811c4',
                    mavenLocalRepo: '.repository',
                    options: [
                            artifactsPublisher(disabled: true),
                            findbugsPublisher(disabled: true),
                            openTasksPublisher(disabled: true)
                    ]
            ) {
                sh "mvn versions:set -DnewVersion=${version} versions:commit -DprocessAllModules=true -q"
            }
}

def sendNotifications(String slackChannel = '', String state = 'STARTED', String extraMessage = '') {

    if (state != 'STARTED') {
        currentBuild.result = state
    }

    notifyBitbucket ignoreUnverifiedSSLPeer: true
    sendNotificationsToSlack(slackChannel, state, extraMessage)
}

def sendNotificationsToSlack(String slackChannel = '', String state = 'STARTED', String extraMessage = '') {

    def COLOR_MAP = [
        'STARTED': '#7CAECE',
        'ABORTED': '#B8B8B8',
        'UNSTABLE': 'warning',
        'SUCCESS': 'good',
        'FAILURE': 'danger'
    ]

    def STATUS_MAP = [
        'STARTED': 'Iniciada :thought_balloon:',
        'ABORTED': 'Interrompida :worried:',
        'UNSTABLE': 'Instável :flushed:',
        'SUCCESS': 'Finalizada com Sucesso :sunglasses:',
        'FAILURE': 'Finalizada com Erro :sob:'
    ]

    def SHOW_CHANGELOG_MAP = [
        'STARTED': true,
        'ABORTED': false,
        'UNSTABLE': false,
        'SUCCESS': false,
        'FAILURE': false
    ]

    def SEND_NOTIFY_CHANNEL_MAP = [
        'STARTED': false,
        'ABORTED': true,
        'UNSTABLE': true,
        'SUCCESS': false,
        'FAILURE': true
    ]

    def color = COLOR_MAP[state]
    def statusMessage = STATUS_MAP[state]

    if (extraMessage.trim() != '') {
        extraMessage = ": (${extraMessage})"
    }

    def message = """*conta-corrente* (${env.MAIN_BRANCH})${extraMessage} - *Deploy para DEV <${env.BUILD_URL}|${env.BUILD_DISPLAY_NAME}>* ${statusMessage}"""

    author = null

    if (SEND_NOTIFY_CHANNEL_MAP[state]) {
        slackSend color: color, message: message, channel: slackChannel
    }
}
