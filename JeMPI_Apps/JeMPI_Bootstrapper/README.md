The app fall under the devops part of JeMPI, and is currently used to manage data used by JeMPI. More specifically, it is used to manage JeMPI postgres, draph, and kafka data. It manages this data through a CLI interface of which the options are list below:

./bootstrapper.sh data -h
```
Usage: <main class> data [-hV] [-c=<config>] [COMMAND]
  -c, --config=<config>   Config file
  -h, --help              Show this help message and exit.
  -V, --version           Print version information and exit.
Commands:
  kafka
  dgraph
  postgres
  resetAll             Deletes all data and schemas associated with JeMPI, then
                         recreates schemas, and add initial data.
  deleteAllSchemaData  Delete all the data and schema used by JeMPI.
  createAllSchemaData  Create all the required schema's and data for JeMPI.
```

./bootstrapper.sh data kafka -h
```
Usage: <main class> data kafka [-hV] [COMMAND]
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  resetAll             Deletes all data and schemas associated with JeMPI kafka
                         instance, then recreates schemas, and add initial data.
  deleteAll            Delete all the data and schema used by JeMPI kafka
                         instance.
  createAllSchemaData  Create all the required schema's and data for JeMPI
                         Kafka instance.
  listTopics           List all the topics associated with the JeMPI instance.
  describeTopic        Describe a topic associated with the JeMPI instance.
```

./bootstrapper.sh data postgres -h
```
Usage: <main class> data postgres [-hV] [COMMAND]
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  resetAll             Deletes all data and schemas associated with JeMPI
                         Postgres instance, then recreates schemas, and add
                         initial data.
  deleteDataOnly       Delete all the data (only) used by JeMPI Postgres
                         instance.
  deleteAll            Delete all the data and schema used by JeMPI Postgres
                         instance.
  createAllSchemaData  Create all the required schema's and data for JeMPI
                         Postgres instance.
```

./bootstrapper.sh data dgraph -h
```
Usage: <main class> data dgraph [-hV] [COMMAND]
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  resetAll             Deletes all data and schemas associated with JeMPI
                         Dgraph instance, then recreates schemas, and add
                         initial data.
  deleteAll            Delete all the data and schema used by JeMPI Dgraph
                         instance.
  createAllSchemaData  Create all the required schema's and data for JeMPI
                         Dgraph instance.
```


**Other notes**

- This application can be run directly (as a java app), or via the script found at devops/linux/bootstrapper.sh (i.e `devops/linux/bootstrapper.sh -h`)

- The app uses the JeMPI environment variable to know what to connect to for the various instances. You can however pass in a config file that contains the variables you want to use instead. These variables in the config file will then be merged with the available environment variables.
    - A sample to the config format can be found here (JeMPI_Apps/JeMPI_Bootstrapper/boostrap.conf.sample)
    - To use this config file you need to specify the config option (i.e `./bootstrapper data resetAll config="<path-to-config>"`)

