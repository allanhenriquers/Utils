instala na m�quina um script para auxiliar na cria��o do arquivo de changelog identificando o <SISTEMA> atr�ves da path do projeto
espera como par�metros:
     a tag de onde se originou a mudan�a que deve ser precedida por '-o' <OLD_TAG> 
     a tag 'nova' (a vers�o do build do jenkins) que deve ser precedida por '-n' <NEW_TAG>

ir� criar um diret�rio oculto na home do usu�rio (~/.script) se n�o houver e dentro deste diret�rio um arquivo chamado changelogger.sh, 
ir� conceder a ele a permiss�o de execu��o e criar� em seu arquivo de perfil (~/.bashrc) um alias para invocar esse script.

invocando o recurso

no repos�torio git de onde deseja identificar as mudan�as abra um terminal execute:
clogger -o <OLD_TAG> -n <NEW_TAG>


� esperado como sa�da algo como:

    Tag do <Nome do Sistema>:
        7.11.13-FF-25.00.05
    Demandas:
        ff-665 Ajuste no relatorio de nota de corretagem
    Entregue em:
        qua dez 4 11:08:47 -03 2019


para instalar baixe o arquivo changeLogger_prepare.sh
de permiss�o de execu��o (sudo chmod +x changeLogger_prepare.sh)
e execute com './changeLogger_prepare.sh'