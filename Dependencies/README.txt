###############

Para instalar as dependências maven necessárias ao projeto da extensão (tipos, interfaces das APIs), abrir a consola nesta pasta e usar os seguintes comandos:

mvn install:install-file -Dfile=pom.xml -DgroupId=pt.landit -DartifactId=landit -Dversion=7.0.0 -Dpackaging=pom -N

mvn install:install-file -Dfile=common-7.0.0.jar -DgroupId=pt.landit -DartifactId=common -Dversion=7.0.0 -Dpackaging=jar -N

mvn install:install-file -Dfile=extensions-7.0.0.jar -DgroupId=pt.landit -DartifactId=extensions -Dversion=7.0.0 -Dpackaging=jar -N

mvn install:install-file -Dfile=extensions-7.0.0-javadoc.jar -DgroupId=pt.landit -DartifactId=extensions -Dversion=7.0.0 -Dpackaging=jar -Dclassifier=javadoc -N

###############

No projeto de front-end (diretoria "frontend" do tempate), instalar o sdk a partir do ficheiro local:

npm install <caminho para o ficheiro landit-extensions-sdk-0.1.0.tgz>

###############
