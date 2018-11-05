---
layout: post
title: Lexica in the The Scala Cite Service (SCS-Akka)
---

# Lexica in the The Scala Cite Service (SCS-Akka)

We have published the [LSJ](http://folio2.furman.edu/lsj/) and [Lewis & Short](http://folio2.furman.edu/lewis-short/) lexica as web-apps drawing their data from a [CEX file](https://github.com/Eumaeus/fuCiteDX/tree/master/Lexica), which is a serialization of two [CITE Collections](https://cite-architecture.github.io).

But some users, scholars, or developers might want direct access to the data coming from that service. This is a guide for accessing the service directly.

A full overview of the SCS-Akka set of microservices for CITE is provided for [the Homer Multitext's Service](http://beta.hpcc.uh.edu/hmt/hmt-microservice/); that service offers texts, collections, and images. The lexica represent a very constrained dataset, so many requests described for the HMT data will return no data from this lexicon service.

## Things You Can Request

### Metadata

- <http://folio2.furman.edu/lex/collections> Definitions of all collections offered by the service.
- <http://folio2.furman.edu/lex/collections/urn:cite2:hmt:lsj.markdown:> The definition of a single collection.

### Retrieval

- <http://folio2.furman.edu/lex/objects/urn:cite2:hmt:lsj.markdown:n109766> Retrieve a single object.
- <http://folio2.furman.edu/lex/objects/urn:cite2:hmt:lsj.markdown:n109760-n109770> Retrieve a range of objects.

### Search

- <http://folio2.furman.edu/lex/objects/find/stringcontains?find=porcupine> Search text in properties across all collections (in this case, both lexica).
- <http://folio2.furman.edu/lex/objects/find/stringcontains/urn:cite2:hmt:lsj.markdown:?find=porcupine> Search text in properties for a single collection.
- <http://folio2.furman.edu/lex/objects/find/regexmatch?find=ἦλθ[οε]ν*> Do a regular expression search for the contents of a CITE Object.

## Output

The output is JSON and should be fairly self-explanatory. There is a [Scala library](https://github.com/cite-architecture/CITE-JSON) for transforming this JSON into CITE object in Scala.

