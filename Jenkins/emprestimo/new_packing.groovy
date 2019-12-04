def repository = 'ssh://git@stash.matera.com:7999/cbank/emprestimo.git'
def slackChannel = '#openbanking-jenkins'

properties(
    [
        disableConcurrentBuilds(),
        parameters(
            [
                string(defaultValue: '', description: 'Versão da release (opcional - se não fornecer, pega do pom). Exemplo: 1.00.00-carbon.01.00', name: 'versao'),
                string(defaultValue: 'c6-v7.11.08', description: 'Branch de onde será gerada a release', name: 'branch'),
                choice(description: 'Tipo de pacote a ser gerado: [test|principal|hotfix] - implica no tipo de versionamento', name: 'type',
                    choices: ["test", "release", "hotfix"].join("\n"))
            ]
        ),
        pipelineTriggers([])
    ]
)

node('k8s-jdk8-maven-builder-slim') {
    if (params.branch == '') {
        error "Branch não informado"
    }
    
    try {


        stage('Checkout') {
            git branch: "${params.branch}", credentialsId: '2a64b1d3-8c92-4ee8-8db0-b766ec6f9c84', url: repository
        }

        stage('Prepare') {


            def pom = readMavenPom()

            def currentVersion = params.versao.trim()


            if (currentVersion == '') {
                currentVersion = pom.version.minus('-SNAPSHOT')
                if (params.type == 'hotfix' && currentVersion.endsWith('.00')){
                    currentVersion = getFirstHotfix(currentVersion)
                }
            }

            validateVersion(currentVersion)



            env.MAIN_BRANCH = "${params.branch}"
            env.CURRENT_VERSION = "${currentVersion}"
            env.TAG = "${currentVersion}"

            if (params.type != "test"){
                nextVersion = getNextVersion(currentVersion, "${params.type}")
                env.NEXT_VERSION = "${nextVersion}-SNAPSHOT"
            }

            wrap([$class: 'BuildUser']) {

                env.BUILD_USER = "${BUILD_USER}"
                env.BUILD_EMAIL = "${BUILD_USER_EMAIL}"

                sh 'git config --global user.email "${BUILD_USER_EMAIL}"'
                sh 'git config --global user.name "${BUILD_USER}"'

            }

        }

        

        stage('Build Package') {
            updateClientVersion(env.CURRENT_VERSION)
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

            if (params.type != "test"){ 
                withMaven(
                        mavenSettingsConfig: '3fa78956-16be-4f78-8c04-b8b7410811c4',
                        mavenLocalRepo: '.repository',
                        options: [
                                artifactsPublisher(disabled: true),
                                findbugsPublisher(disabled: true),
                                openTasksPublisher(disabled: true)
                        ]
                ) {
                    sh 'mvn clean deploy -DskipTests -f api-client/pom.xml'
                }
                sh 'git reset --hard'
            }  else {
                println "Para empacotamentos de Teste não se faz 'deploy' de api-client"
            }

        }

        stage('Build Database') {
            def currentVersion = "${env.CURRENT_VERSION}"
            println "Current version = $currentVersion"
            

                    
            configFileProvider([configFile(fileId: 'gradle-mirror', targetLocation: '/root/.gradle/init.gradle')]) {
                sh "./gradlew clean buildBackend -Pend=HEAD -Pversion=${currentVersion} --refresh-dependencies --stacktrace -Dwrap.local=false"
            }
            archive "**/*.zip"

        }
        
        stage('Next Version') {

            if (params.type != "test"){

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

            } else {
                println "Para empacotamentos de Teste não se altera versão"
                currentBuild.description = "Versão ${env.CURRENT_VERSION}-SNAPSHOT"
            }

        }

        stage('Tag') {

            if (params.type != "test"){ 

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

                    if [ -z "$CUSTOM_TAG" ]
                    then
                        sh 'git tag -a ${CUSTOM_TAG}'
                        sh ''

                }  
            } else {
                println "Para empacotamentos de teste não coloca TAGs em commits"
            }
        }

    } catch (ignored) {
        currentBuild.result = 'FAILURE'
    } finally {
        sendNotificationsToSlack(slackChannel, currentBuild.currentResult, "emprestimos")
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
                
                sh "mvn versions:set -DnewVersion=${version} versions:commit -DprocessAllModules=true -q -Pboot"
            }
}



def validateVersion(cod) {
    format = "\\d\\.\\d{2}\\.\\d{2}-carbon\\.\\d{2}\\.\\d{2}"
    if (!cod.matches(format)){
        msg = "Versão em formato inadequado ${cod} =~ X.XX.XX-carbon.XX.XX"
        println msg
        error msg
    } 
    println "Verificada versao -> formato adequado"
}

def updateClientVersion(version) {
    versionFilePath = 'client/src/main/resources/version.properties'  
    println "Alterando propriedade version do arquivo ${versionFilePath} para ${version}"
    sh "sed -i s:^\\s*version\\s*=.*\$:version=${version}:g ${versionFilePath}"
}

def getNextVersion(currentVersion, type){
    bigParts = currentVersion.split("-")
    smallParts = bigParts[1].split("\\.")


    if (type == "release"){
        println "=== starting a build of type RELEASE ==="
        bigParts[0] + "-" + smallParts[0] + "." + intTo2Digits(smallParts[1].toInteger() + 1) + ".00"
    }
    else if (type == "hotfix") {
        println "=== starting a build of type HOTFIX ==="
        bigParts[0] + "-" + smallParts[0] + "." + smallParts[1] + "." + intTo2Digits(smallParts[2].toInteger() + 1)
    }
    else {
        msg = "Tipo de build invalido - $type"
        println msg
        error msg
    }
}

def getFirstHotfix(currentVersion){
    bigParts = currentVersion.split("-")
    smallParts = bigParts[1].split("\\.")

    bigParts[0] + "-" + smallParts[0] + "." + intTo2Digits(smallParts[1].toInteger() - 1) + ".01"    
}

def intTo2Digits(Integer num){
    if (num.toString().length() == 1){
        "0$num"
    } else{
        num.toString()
    }
}

def sendNotifications(String slackChannel = '', String state = 'STARTED', String extraMessage = '', String sistema) {

    if (state != 'STARTED') {
        currentBuild.result = state
    }

    notifyBitbucket ignoreUnverifiedSSLPeer: true
    sendNotificationsToSlack(slackChannel, state, extraMessage, sistema)
}

def sendNotificationsToSlack(String slackChannel = '', String state = 'STARTED', String extraMessage = '', String sistema) {

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

    def message = """*$sistema* (${env.MAIN_BRANCH})${extraMessage} - *Deploy para DEV <${env.BUILD_URL}|${env.BUILD_DISPLAY_NAME}>* ${statusMessage}"""

    author = null

    if (SEND_NOTIFY_CHANNEL_MAP[state]) {
        slackSend color: color, message: message, channel: slackChannel
    }
}
