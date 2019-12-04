instala na máquina um script para auxiliar na criação do arquivo de changelog identificando o <SISTEMA> atráves da path do projeto
espera como parâmetros:
     a tag de onde se originou a mudança que deve ser precedida por '-o' <OLD_TAG> 
     a tag 'nova' (a versão do build do jenkins) que deve ser precedida por '-n' <NEW_TAG>

irá criar um diretório oculto na home do usuário (~/.script) se não houver e dentro deste diretório um arquivo chamado changelogger.sh, 
irá conceder a ele a permissão de execução e criará em seu arquivo de perfil (~/.bashrc) um alias para invocar esse script.

invocando o recurso

no reposítorio git de onde deseja identificar as mudanças abra um terminal execute:
clogger -o <OLD_TAG> -n <NEW_TAG>


é esperado como saída algo como:

    Tag do <Nome do Sistema>:
        7.11.13-FF-25.00.05
    Demandas:
        ff-665 Ajuste no relatorio de nota de corretagem
    Entregue em:
        qua dez 4 11:08:47 -03 2019


para instalar baixe o arquivo changeLogger_prepare.sh
de permissão de execução (sudo chmod +x changeLogger_prepare.sh)
e execute com './changeLogger_prepare.sh'