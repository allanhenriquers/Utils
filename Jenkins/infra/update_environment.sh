#!/bin/zsh
SYSTEM=`$0`
VERSION=`$1`
FIX=`$2`

# REGEX_PATH=\d{1,}\.\d{1,}\.\d{1,}

# sudo su matera

# cd /matera/appl

# rm $SYSTEM-bkp

# sudo systemctl stop tomcat-legados &

# mv $SYSTEM/ $SYSTEM-bkp

# mkdir $SYSTEM

# cd $SYSTEM


# defineURI( $SYSTEM ) {
echo $SYSTEM
if $SYSTEM eq 'emprestimo'
then
    $NEW_RELEASE='New_Release'
else
    $NEW_RELEASE='New%20Release'
fi
# echo $NEW_RELEASE


# http://jenkins.matera.com/job/projetos/job/Varejo/job/C6-Bank/job/$SYSTEM/job/New%20Release/lastSuccessfulBuild/artifact/webapp/target/cartoes.war
# http://jenkins.matera.com/job/projetos/job/Varejo/job/C6-Bank/job/$SYSTEM/job/New%20Release/lastSuccessfulBuild/artifact/build/plsql/7.11.04/Final/car_7.11.04.zip
# http://jenkins.matera.com/job/projetos/job/Varejo/job/C6-Bank/job/$SYSTEM/job/New%20Release/lastSuccessfulBuild/artifact/webapp/target/sdcompe.war
# http://jenkins.matera.com/job/projetos/job/Varejo/job/C6-Bank/job/$SYSTEM/job/New%20Release/lastSuccessfulBuild/artifact/build/plsql/7.11.05/Final/cmp_7.11.05.zip
# http://jenkins.matera.com/job/projetos/job/Varejo/job/C6-Bank/job/$SYSTEM/job/New%20Release/lastSuccessfulBuild/artifact/webapp/target/convenios.war
# http://jenkins.matera.com/job/projetos/job/Varejo/job/C6-Bank/job/$SYSTEM/job/New%20Release/lastSuccessfulBuild/artifact/build/plsql/7.11.13/Final/cnv_7.11.13.zip
# http://jenkins.matera.com/job/projetos/job/Varejo/job/C6-Bank/job/$SYSTEM/job/New%20Release/lastSuccessfulBuild/artifact/webapp/target/sdconta.war
# http://jenkins.matera.com/job/projetos/job/Varejo/job/C6-Bank/job/$SYSTEM/job/New%20Release/lastSuccessfulBuild/artifact/build/plsql/7.11.15/Final/cta_7.11.15.zip
# http://jenkins.matera.com/job/projetos/job/Varejo/job/C6-Bank/job/$SYSTEM/job/New_Release/lastSuccessfulBuild/artifact/webapp/target/sdemp.war
# http://jenkins.matera.com/job/projetos/job/Varejo/job/C6-Bank/job/$SYSTEM/job/New_Release/lastSuccessfulBuild/artifact/build/plsql/7.11.08/Final/emp_7.11.08.zip
# http://jenkins.matera.com/job/projetos/job/Varejo/job/C6-Bank/job/$SYSTEM/job/New%20Release/lastSuccessfulBuild/artifact/webapp/target/garantias.war
# http://jenkins.matera.com/job/projetos/job/Varejo/job/C6-Bank/job/$SYSTEM/job/New%20Release/lastSuccessfulBuild/artifact/build/plsql/7.11.02/Final/gt_7.11.02.zip
# http://jenkins.matera.com/job/projetos/job/Varejo/job/C6-Bank/job/$SYSTEM/job/New%20Release/lastSuccessfulBuild/artifact/webapp/target/sdopen.war
# http://jenkins.matera.com/job/projetos/job/Varejo/job/C6-Bank/job/$SYSTEM/job/New%20Release/lastSuccessfulBuild/artifact/build/plsql/7.11.11/Final/opn_7.11.11.zip


