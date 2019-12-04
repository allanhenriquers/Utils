
class teste {





    static void main(args) {
       String CUSTOM_TAG_EMPTY='     ';
       String CUSTOM_TAG='vnn-latest      vnn-latest               vnn-latest';
       String TAG_CONTENT='';
       String v = '7.11.00';

        println TAG_CONTENT;
        println (CUSTOM_TAG_EMPTY?.trim());
        // println CUSTOM_TAG_EMPTY;
        println (CUSTOM_TAG?.trim());
        // println CUSTOM_TAG;


       // getTagContent();
   }


   // static def getTagContent(String v) {
   //  CURRENT_VERSION=v;
   //  TAG_CONTENT=sh 'git log --pretty=format:"%s" test-latest...7.11.00 | grep -v -e "Updating" -e "Merging" -e "Releasing" -e "Merge"';
   //  println TAG_CONTENT;
   //      // println (CUSTOM_TAG_EMPTY?.trim());
   //      println CUSTOM_TAG_EMPTY;
   //      // println (CUSTOM_TAG?.trim());
   //      println CUSTOM_TAG;
   //  }


}
