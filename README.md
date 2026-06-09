# vmax_multi_source

SPARQL middleware that aggregates multiple VMAX plugins into a single Fuseki SPARQL endpoint.

## Setup

Copy `config.jsonc` to `config.json` and adjust port and plugin list:

```json
{
    "main-port": 3030,
    "plugins": [
        {
            "plugin-name": "my-plugin",
            "plugin-port": 8080
        }
    ]
}
```

## Run

Requires Java 17+ and Maven.

```bash
mvn compile exec:java
```

The SPARQL endpoint is then available at `http://localhost:3030/data/query`.

## Query example

```bash
curl -G http://localhost:3030/data/query \
  --data-urlencode "query=SELECT * WHERE { ?s ?p ?o }"
```
