#!/bin/sh


while getopts ":o:d:" opt; do
  case $opt in
    o) OLD_TAG="$OPTARG"
    ;;
    d) NEW_TAG="$OPTARG"
    ;;
    \?) echo "Invalid option -$OPTARG" >&2
    ;;
  esac
done


# OLD_TAG='7.11.13-c6-25.00.04'
# NEW_TAG='7.11.13-c6-25.00.05'
SISTEMA='Renda-Fixa'
LOG_PATH='./CHANGELOG.md'
# GIT_PATH='/home/ahmr/projetos/matera/renda-fixa'

echo 'Tag do '${SISTEMA}':\n    '${NEW_TAG}'\nDemandas:'  > ${LOG_PATH}

# cd $GIT_PATH

git log --pretty=format:"    %s" ${OLD_TAG}..${NEW_TAG} | grep -v -e "Updating" \
-e "Merging" -e "Releasing" -e "Merge" -e "Automati"  >> ${LOG_PATH}

echo 'Entregue em:\n''    '`date` >> ${LOG_PATH}

