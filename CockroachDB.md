## CockroachDB
I'm going to use cockroachDB, just because I've used Mysql and Postgres before and what to try something new.

### Installation (simple non-production)
As I'm only tinkering about, I'll do a simple single node with no security - I might revisit this later and add
security certs.

### Windows:
- Start a powershell
- PS $ErrorActionPreference = "Stop"; [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12;$ProgressPreference = 'SilentlyContinue'; $null = New-Item -Type Directory -Force $env:appdata/cockroach; Invoke-WebRequest -Uri https://binaries.cockroachdb.com/cockroach-v22.1.0-beta.5.windows-6.2-amd64.zip -OutFile cockroach.zip; Expand-Archive -Force -Path cockroach.zip; Copy-Item -Force "cockroach/cockroach-v22.1.0-beta.5.windows-6.2-amd64/cockroach.exe" -Destination $env:appdata/cockroach; $Env:PATH += ";$env:appdata/cockroach"

### MacOS:
- Start a Terminal
- brew install cockroachdb/tap/cockroach

### Trying CockroachDB out
First I'll start the server and then use a terminal based client.

#### Manually starting the server (insecure)
Now we can start a simple insecure dev node instance with:
- From a powershell/terminal
- cockroach start-single-node --insecure
- Note that you may need to accept a Windows defender use of a port(Windows).

#### Manually connecting to the server (insecure)
Just to see if we can create some tables and connect to the server:
- From a powershell/terminal
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

### That'll do for the time being
I'll probably define a whole load of tables in the future and try using Spring JPA to access them.