# README #

### What is this repository for? ###

* REST layer of CSP

### How do I get set up? ###
To build project:

```
#!java

./gradlew clean build

```

To run project:

```
#!java

./gradlew flywayMigrate -i && ./gradlew bootRun -Dserver.port=8080

```
### Migration: ###
If you want to create new table migration follow next steps:

1) Go to /src/main/resources/db/migration

2) Create new migration sql file with name 
```V1_<version_migration>__<name_of_migration>.sql```
``` Example: V1_0__InitDataBase.sql```

__Note__: If you don't set 2 bottom line after *version_migration*, migration won't work.

3) Run : ``` ./gradlew flywayMigrate -i ```, or you can run 
 ``` ./gradlew flywayMigrate -i && ./gradlew bootRun -Dserver.port=8080 ``` and start server after migration.
 
__Note__: If you already had created database which was created without flyway, then you must remove all tables on your schema^ and the run command(which described above) in termminal.

### Execute SQL files ###
dbInstall -DdbFiles=[List of files]
Example:
```./gradlew clean dbInstall build bootRun -Dserver.port=8080 -DdbFiles=therapist,users```
Note:
 The directory of SQL dumps is resources/db.test
 Check "Use insert commands" if you want to create own dumps.
 Run ```dbInstall``` task before using build/bootRun. You can run this command apart using grant:
 ```grant dbInstall -DdbFiles=therapist```

###Run command###

Also you can update configs in when you run or build project throw terminal.
Configs which you can update:


Command            |    Description    |    Default  
-------------------|-------------------|-----------------------------------
-Dserver.port      | Server port       |     9090    
-Dversion          | Build Version     |     0.7.3 
-Demail            | Email host        |  the-diabetes-cure.netfirms.com   
-Ddb.user          | Databse username  |  csp   
-Ddb.pass          | Database pasword  |  9A6k3F6m   
-Ddb.port          | Database port     |  5432   
-Ddb.name          | Database name     |  csp   
-Ddb.host          | Database host     |  localhost   
-Dconf / -Pconf    | URL confFile      |  application.yml 
   
Example:

```
 -Dversion=1.0.0
```

Get application version:

```
#!java

/api/version

```

### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines