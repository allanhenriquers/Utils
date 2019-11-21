String CURRENT_VERSION = '7.11.10.00-gsx-fix-c6-31'

def pomVersionPattern = "^((?:\\d+\\.){2}\\d+).*"

def versionDb
if(CURRENT_VERSION.matches(pomVersionPattern)) {
    versionDb = CURRENT_VERSION.replaceAll(pomVersionPattern, '\$1') 

}
println versionDb

// def versionDb = CURRENT_VERSION.substring(0, CURRENT_VERSION.indexOf('.\\d\\d-\\w*'))

// println versionDb


// Console console=System.console();
// def name=console.readLine("What is your name? ")
// println "Welcome to Groovy, $name!"




// def email = 'groovy@gmail.com'
 
// def exp =  /@\\w*/
 
// println email ==~ exp
// println email.indexOf(exp)