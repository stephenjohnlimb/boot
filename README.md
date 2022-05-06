## Spring Boot Example
### Purpose
Just a quick check of the processes in getting a Spring Boot app up running and pushed to gitHub via IntelliJ.

### How
Used [Spring initializr](https://start.spring.io/) to create a simple app, could have used a maven archetype, or just done it all by hand.

### Add in some REST
The next plan is to add in some simple REST and then maybe employ CockroachDb to try that out.

### Planning to use CockroachDB

#### Installation (simple non-production)
So for windows:
- Start a powershell
- PS $ErrorActionPreference = "Stop"; [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12;$ProgressPreference = 'SilentlyContinue'; $null = New-Item -Type Directory -Force $env:appdata/cockroach; Invoke-WebRequest -Uri https://binaries.cockroachdb.com/cockroach-v22.1.0-beta.5.windows-6.2-amd64.zip -OutFile cockroach.zip; Expand-Archive -Force -Path cockroach.zip; Copy-Item -Force "cockroach/cockroach-v22.1.0-beta.5.windows-6.2-amd64/cockroach.exe" -Destination $env:appdata/cockroach; $Env:PATH += ";$env:appdata/cockroach"

#### Manually starting the server (insecure)
Now we can start a simple insecure dev node instance with:
- From a powershell
- cockroach start-single-node --insecure
- Note that you may need to accept a Windows defender use of a port.

#### Manually connecting to the server (insecure)
Just to see if we can create some tables and connect to the server:
- From a powershell
- cockroach sql --insecure

This will connect you to the running cockroach server.

#### Check tables and create a sample
- show tables

Now create a table
```
root@:26257/defaultdb> CREATE TABLE test (
user_id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
username VARCHAR(50) UNIQUE NOT NULL,
password VARCHAR(50) NOT NULL,
email VARCHAR(255) UNIQUE NOT NULL
);
```

To show the details of the table use `\d test`.

#### Try some data in the test table
```
insert into test(username, password, email) values('Steve', 'Limb', 'stephenjohnlimb@gmail.com');
```

Query that table:
```
 select * from test;
```

You should get something back like:
```
                user_id                | username | password |           email
---------------------------------------+----------+----------+----------------------------
  1cfb7b0e-6df7-49ae-9cfc-184ff30c3e97 | Steve    | Limb     | stephenjohnlimb@gmail.com
(1 row)
```