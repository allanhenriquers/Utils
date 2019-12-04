instala na m�quina um script para auxiliar na cria��o de tags de ultima vers�o
espera como par�metros:
    **esta flag � obrigat�ria apenas se for informado mais de uma par�metro**
    a tag ou branch que ser� marcada como a mais recente indicada por '-v' <VERSAO> 
    **esta flag pode ser ignorada, neste caso ser� passado como reposit�rio alvo o padr�o do git: "origin"**
    o nome do repositorio quando houver mais de um vinculado ao seu projeto '-r' <REPO>

ir� criar um diret�rio oculto na home do usu�rio (~/.script) se n�o houver e dentro deste diret�rio um arquivo chamado update_tag.sh, 
ir� conceder a ele a permiss�o de execu��o e criar� em seu arquivo de perfil (~/.bashrc) um alias para invocar esse script.

**invocando o recurso**
**importante � necessario estar na branch ou tag que ser� indicada como a nova latest**
no repos�torio git de onde deseja identificar as mudan�as abra um terminal execute:
change_latest -v <vx> -r <ORIGIN>

ou usando a forma simplificada

change_latest <vx>

o script est� preparado para trabalhar com inser��o manual* das informa��es para a tag
e deve excluir a tag vx-latest local e do servidor remoto, e recriar local e remotamente com as novas informa��es 


para instalar baixe o arquivo tagging_prepare.sh
de permiss�o de execu��o (sudo chmod +x tagging_prepare.sh)
e execute com './tagging_prepare.sh'

� poss�vel com a mudan�a na forma da cria��o da tag (um pr�ximo passo)
git tag -a <NOME_DA_TAG> --> para git tag -m <UM_ARQUIVO> fazer o "tagueamento" sem a entrada manual de informa��es