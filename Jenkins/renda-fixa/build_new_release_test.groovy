
Class build_new_release_groovy {

static main(args) {
    testRegex();
}


    public static String testRegex() {

        def CURRENT_VERSION = '7.11.10.00-gsx-fix-c6-31'

        echo CURRENT_VERSION.indexOf('.\\d\\d-\\w*')

        def versionDb = CURRENT_VERSION.substring(0, CURRENT_VERSION.indexOf('.\\d\\d-\\w*'))

        println versionDb

    }

}