def repository = 'ssh://git@stash.matera.com:7999/cbank/cadastro-central.git'
def slackChannel = '#openbanking-jenkins'

properties(
    [
        disableConcurrentBuilds(),
        parameters(
            [
                string(defaultValue: '', description: 'Versão atual (opcional). Exemplo: 1.0.0-BUILD01', name: 'versaoAtual'),
                string(defaultValue: '', description: 'Próxima versão (opcional). Exemplo: 1.0.0-BUILD02', name: 'proximaVersao'),
                string(defaultValue: 'c6-v7.11.11', description: 'Branch de onde será gerada a release', name: 'branch')
            ]
        ),
        pipelineTriggers([])
    ]
)

node('jdk8-maven-slim') {
    if (params.branch == '') {
        error "Branch não informado"
    }
    
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
            
            if (currentVersion.contains('carbon')){
                msg = "Este build não é adequado para projetos que já mudaram o modelo de versionamento..."
                println msg
                error msg
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

        // stage('Tag') {

        //     sshagent(['2a64b1d3-8c92-4ee8-8db0-b766ec6f9c84']) {

        //         // Branch
        //         sh 'git checkout ${MAIN_BRANCH}'
        //         sh 'git pull origin ${MAIN_BRANCH}'

        //         // Versão do Release
        //         sh 'git checkout -b release/${TAG}'
        //         updateVersionWithMaven(env.CURRENT_VERSION)

        //         sh 'git commit -a -m "Releasing ${TAG} from ${MAIN_BRANCH} by ${BUILD_USER} through Jenkins (${BUILD_URL})"'

        //         sh 'git tag $TAG'
        //         sh 'git push origin $TAG'

        //     }

        // }

        // stage('Build Package') {
        //     updateClientVersion()

        //     withMaven(
        //             mavenSettingsConfig: '3fa78956-16be-4f78-8c04-b8b7410811c4',
        //             mavenLocalRepo: '.repository',
        //             options: [
        //                     artifactsPublisher(disabled: false),
        //                     findbugsPublisher(disabled: true),
        //                     openTasksPublisher(disabled: true)
        //             ]
        //     ) {
        //         withCredentials([file(credentialsId: 'MATERA_KEYSTORE', variable: 'KEYSTORE')]) {
        //             env.MATERA_KEYSTORE = "${KEYSTORE}"
        //             sh 'mvn clean install -Dwebstart.timestamp.url=http://time.certum.pl'
        //         }
        //     }
        //     archive "**/target/*.war"
        //     sh 'git reset --hard'
        // }

        // stage('Build Database') {
        //     def currentVersion = "${env.CURRENT_VERSION}"
        //     echo 'Current version = ' + currentVersion
        //     def versionDb = currentVersion.substring(0, currentVersion.indexOf('-c6'))
        //     echo 'Current version = ' + versionDb
        //     configFileProvider([configFile(fileId: 'gradle-mirror', targetLocation: '/root/.gradle/init.gradle')]) {
        //         sh "./gradlew clean buildBackend -Pend=HEAD -Pversion=${versionDb} --refresh-dependencies --stacktrace -Dwrap.local=false"
        //     }
        //     archive "**/*.zip"

        // }
        getTagContent(env.CURRENT_VERSION)

        stage('Tagging Latest') {
           if ( ${CUSTOM_TAG}?.trim() ) {
                sh 'git tag --delete origin ${CUSTOM_TAG}'
                sh 'git tag ${CUSTOM_TAG} -m ${TAG_CONTENT}'
                sh 'git pull origin ${CUSTOM_TAG}'
            }
        }
        
        // stage('Next Version') {

        //     sshagent(['2a64b1d3-8c92-4ee8-8db0-b766ec6f9c84']) {

        //         // Merge no branch
        //         sh 'git checkout ${MAIN_BRANCH}'
        //         sh 'git merge --no-ff -X theirs -m "Merging release/${TAG} into ${MAIN_BRANCH} using theirs strategy" release/${TAG}'
        //         // Próxima versão
        //         updateVersionWithMaven(env.NEXT_VERSION)
        //         sh 'git commit -a -m "Updating to next version: ${NEXT_VERSION}"'
        //         sh 'git push origin ${MAIN_BRANCH}'
        //     }

        //     currentBuild.description = "Versão ${env.CURRENT_VERSION}"
        // }
    } catch (ignored) {
        currentBuild.result = 'FAILURE'
    } finally {
        sendNotificationsToSlack(slackChannel, currentBuild.currentResult)
    }
}

def getTagContent(env.CURRENT_VERSION) {
    TAG_CONTENT=(sh 'git log --pretty=format:"%s" test-latest...7.11.08.00-c6-18 | grep -v -e "Updating" -e "Merging" -e "Releasing" -e "Merge"')
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
                
                sh "mvn versions:set -DnewVersion=${version} versions:commit -q"
                sh "mvn versions:set -DnewVersion=${version} versions:commit -q -f api-client/pom.xml"
                sh "mvn versions:set -DnewVersion=${version} versions:commit -q -f business/pom.xml"
                sh "mvn versions:set -DnewVersion=${version} versions:commit -q -f boot/pom.xml"
                sh "mvn versions:set -DnewVersion=${version} versions:commit -q -f client/pom.xml"
                sh "mvn versions:set -DnewVersion=${version} versions:commit -q -f connector-rest/pom.xml"
                sh "mvn versions:set -DnewVersion=${version} versions:commit -q -f domain/pom.xml"
                sh "mvn versions:set -DnewVersion=${version} versions:commit -q -f resources/pom.xml"
                sh "mvn versions:set -DnewVersion=${version} versions:commit -q -f persistence/pom.xml"
                sh "mvn versions:set -DnewVersion=${version} versions:commit -q -f service/pom.xml"
                sh "mvn versions:set -DnewVersion=${version} versions:commit -q -f webapp/pom.xml"
            }
}

def updateClientVersion() {
    def versionFilePath = 'client/src/main/resources/version.properties'
    
    println "Alterando propriedade version do arquivo ${versionFilePath}"

    def version = CURRENT_VERSION;
    def versionFormatted = version.substring(0, version.indexOf('-c6'))
    def versionForProperties = versionFormatted + '.00'

    sh "sed -i 's:^[ \\t]*version[ \\t]*=\\([ \t]*.*\\)[/]*\$:version='${versionForProperties}':' ${versionFilePath}"
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

    def message = """*cadastro-central* (${env.MAIN_BRANCH})${extraMessage} - *Deploy para DEV <${env.BUILD_URL}|${env.BUILD_DISPLAY_NAME}>* ${statusMessage}"""

    author = null

    if (SEND_NOTIFY_CHANNEL_MAP[state]) {
        slackSend color: color, message: message, channel: slackChannel
    }
}
