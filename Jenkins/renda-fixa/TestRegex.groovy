String CURRENT_VERSION = '7.11.10.00-gsx-fix-c6-31'
String pattern = ~/.(\d{1,2})\-(\w*)/

println pattern

println CURRENT_VERSION =~ pattern
println CURRENT_VERSION.indexOf('.00-gsx')
// .indexOf('.\\d\\d-\\w*')

// def versionDb = CURRENT_VERSION.substring(0, CURRENT_VERSION.indexOf('.\\d\\d-\\w*'))

// println versionDb


// Console console=System.console();
// def name=console.readLine("What is your name? ")
// println "Welcome to Groovy, $name!"




// def email = 'groovy@gmail.com'
 
// def exp =  /@\\w*/
 
// println email ==~ exp
// println email.indexOf(exp)