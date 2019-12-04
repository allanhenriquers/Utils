instala na máquina um script para auxiliar na criação de tags de ultima versão
espera como parâmetros:
    **esta flag é obrigatória apenas se for informado mais de uma parâmetro**
    a tag ou branch que será marcada como a mais recente indicada por '-v' <VERSAO> 
    **esta flag pode ser ignorada, neste caso será passado como repositório alvo o padrão do git: "origin"**
    o nome do repositorio quando houver mais de um vinculado ao seu projeto '-r' <REPO>

irá criar um diretório oculto na home do usuário (~/.script) se não houver e dentro deste diretório um arquivo chamado update_tag.sh, 
irá conceder a ele a permissão de execução e criará em seu arquivo de perfil (~/.bashrc) um alias para invocar esse script.

**invocando o recurso**
**importante é necessario estar na branch ou tag que será indicada como a nova latest**
no reposítorio git de onde deseja identificar as mudanças abra um terminal execute:
change_latest -v <vx> -r <ORIGIN>

ou usando a forma simplificada

change_latest <vx>

o script está preparado para trabalhar com inserção manual* das informações para a tag
e deve excluir a tag vx-latest local e do servidor remoto, e recriar local e remotamente com as novas informações 


para instalar baixe o arquivo tagging_prepare.sh
de permissão de execução (sudo chmod +x tagging_prepare.sh)
e execute com './tagging_prepare.sh'

é possível com a mudança na forma da criação da tag (um próximo passo)
git tag -a <NOME_DA_TAG> --> para git tag -m <UM_ARQUIVO> fazer o "tagueamento" sem a entrada manual de informações