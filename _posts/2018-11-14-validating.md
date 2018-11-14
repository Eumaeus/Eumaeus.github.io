---
layout: post
title: Testing Lexica
---

# How Many Entries in Liddell-Scott?

Earlier, [I announced a new online version of the Liddell, Scott, Jones Greek Lexicon](https://eumaeus.github.io/2018/10/30/lsj.html), based on [a transformation of the Perseus *LSJ* data, by Giuseppe Celano](https://github.com/gcelano/LSJ_GreekUnicode). Subsequently, I announced a [new version of the lexicon data based on further work at the University of Chicago](https://eumaeus.github.io/2018/11/05/chicago.html).

Professor Helma Dik, who had shared the Chicago data with me, [asked if I had done a count of entries in the *LSJ*](https://twitter.com/LogeionGkLat/status/1060182402272841728).

The [CITE Architecture](http://cite-architecture.github.io) makes counting things easy, because it is [entirely based on canonical citation of the objects scholars study](http://cite-architecture.github.io/about/whycitation/). At the time, I was able to report the following: The CEX transformation of the origional Perseus Project XML of the LSJ contained **116,501** entries. The Chicago version contained **116,435** entries.

Now, the 19th century *LSJ* is neither a machine-actionable database of Greek lexicography, nor a definitive catalogue of Greek lexemes. Typing that big book into XML, CEX, or any other electronic format does not change this reality. *LSJ* is a selective list intended for human readers.

But it would be nice to know what is in that list, so I have undertaken to do a little more work to understand why the original Perseus *LSJ* and the Chicago version contain a different number of entries. The result is a [new version of the Chicago data](https://github.com/Eumaeus/cite_lsj_cex) with a number of errors corrected. The final tally is **116,854** entries.

Below is a sketchy description of how I went about testing the *LSJ* data. I do not include full sourcecode here, since doing so would require explaining where a bunch of files are in a local filesystem. If anyone cares, I will happily send a DropBox link to the complete setup (this was enough of a one-off that I did not do a full GitHub repository).

All tests were run using [SBT](https://www.scala-sbt.org) and [Scala](https://www.scala-lang.org), with various [CITE Libraries](https://github.com/cite-architecture).


## Duplicate ID Values?

Are there duplicate ID values in either the Perseus or Chicago versions? I read the CEX data lines into a Vector of Strings, *e.g.*:

~~~ scala
val persStr = Source.fromFile("perseus_lsj.cex").getLines.toVector
~~~

I mapped those lines to a Vector of `Tuples(Cite2Urn,String)`, where the `String` is the lemma:

~~~ scala
val perseusData:Vector[(Cite2Urn,String)] = persStr.map( l => {
	val urn:Cite2Urn = Cite2Urn(l.split("#")(1))
	val lemma:String = l.split("#")(2)
	(urn,lemma)		
})
~~~

I grouped those items according to their URN values, and filtered out any groups with only one member; what was left were a set of entries with duplicate URNs:

~~~ scala
val perseusDuplicates:Map[Cite2Urn,Vector[(Cite2Urn,String)]] = {
	perseusData.groupBy(_._1)
} filter (_._2.size > 1)
~~~

On the first pass, the Perseus data had no duplicate URNs; the Chicago data had 43, with one URN being shared among three entries.

> **N.b.** The Scala `citeobj` library, for which I was responsible for the most recent testing, completely failed to catch these! Neel Smith and I have since updated `citeobj` to 7.1.3, with new unit-tests, confirming that CEX collection data with duplicate URNs will throw an exception: <https://github.com/cite-architecture/citeobj>.

### Remedy

- Back in the XML files from Chicago, I found the single triplicate URN (`urn:cite2:hmt:lsj.chicago_md:n5679`) and edited those three entries' ID vallues to `n5679`, `n5679a`, `n5679b`. 
- I edited the Scala script that processes the 86 XML files to catch duplicates and give the second item an ID ending in `a`.
- I regenerated and tested again.
- I confirmed that neither lexicon now has duplicate IDs.

## Duplicate Lemmata?

To see if there were duplicated *lemmata* in the lexica, I mapped our data-vector, grouping by lemma this time, filtering out those with only one member:

~~~ scala
val perseusLemmas:Map[String,Vector[(Cite2Urn,String)]] = {
	perseusData.groupBy( _._2)
} filter (_._2.size > 1)
~~~

On the first pass, the Perseus data had no duplicate lemmas. The Chicago data had 513, which is more than I wanted to deal with by hand. What is going on here?

I checked to see if there are instances where the lemma _and_ the entry's text are the same:

~~~ scala
val chicagoLemmas:Map[String,Vector[(Cite2Urn,String,String)]] = {
	chiStr.map(l => {
		val splitted:Array[String] = l.split("#")
		(Cite2Urn(splitted(1)), splitted(2), splitted(3))
	}).groupBy( _._2)
} filter (_._2.size > 1)

val duplicateLemmaAndEntries:Int = chicagoLemmas.map(_._2).toVector.flatten.groupBy(_._3).filter(_._2.size > 1).size
~~~

This showed me that there are no instances of identical lemmas _and_ entries.

## IDs present in One and Not in the Other?

I wanted a list of URNs to entries for which the CITE2 URN's `objectSelector`, the ID of the entry itself, is present in one lexicon but not in another. 

~~~ scala
val perseusIds:Vector[String] = perseusData.map(_._1.objectComponent)
val chicagoIds:Vector[String] = chicagoData.map(_._1.objectComponent)

val perseusHapax:Vector[String] = {
	val unwanted = chicagoIds.toSet
	perseusIds.filterNot(unwanted)
} 
~~~

I found that there were 476 IDs that are in the Perseus *LSJ* but not in the Chicago version. Visual inspection showed that these are the "glosses", short associations of one Greek word to another, taken from Byzantine manuscripts.

I found that there were 410 IDs that are present in the Chicago *LSJ* but not in the Perseus one. Of these, 72 have IDs in `fu…x`, *e.g.* `urn:cite2:hmt:lsj.chicago_md:fu79353x` ("παρελκ-υ<σ>τής"). These are entries added by the Chicago team that did not have an `@orig_id` attribute in the XML, so my script that generated the CEX created one. 

303 entries have an `a` at the end of the ID, and 26 have a `b`; these were automatically created by the script to disambiguate individual entries in Perseus that Chicago divided into two or three discrete entries. So all the entries identified in Chicago but not in Perseus are the result of deliberate scholarly editing.

It is possible that some of those, especially the `fuNNx` ones, are actually present in the Perseus data, but with different IDs. So I checked the lemmata on these. Because Chicago did a lot of work on the Unicode, I normalized the lemmata and removed markup characters like hyphens before comparing.

~~~ scala
// Get a Vector where the lemmata are Normalized lemmata from the Perseus Hapax Data (phdNormal)
val phdNormal:Vector[(Cite2Urn,String)] = {
	perseusHapaxData.filter( phd => {
		val phdNormString:String = normalize(phd._2).replaceAll("[-<>·]","").toLowerCase
		chicagoHapaxData.filter(chd => {
			normalize(chd._2).replaceAll("[-<>·]","").toLowerCase == phdNormString
		}).size > 0
	})
}

// pair those up
val hapaxMatches:Vector[((Cite2Urn,String),(Cite2Urn,String))] = {
	phdNormal.map( pn => {
		val thisString = normalize(pn._2).replaceAll("[-<>·]","").toLowerCase
		val chiUrn:Cite2Urn = chicagoHapaxData.filter( chd => {
			normalize(chd._2).replaceAll("[-<>·]","").toLowerCase == thisString
		})(0)._1
		val chiObject = chicagoData.find(_._1 == chiUrn).get
		(pn,chiObject)
	})
}
~~~

This resulted in 57 matched pairs… entries that are present in both Perseus data and Chicago data, but with different URNs. The one exception is a pair of entries for χράομαι, `urn:cite2:hmt:lsj.markdown:n114548` and `urn:cite2:hmt:lsj.chicago_md:n114553a`. In the Perseus *LSJ*, that entry's content is: “χράομαι, `A` v. χράω (B) C.” The Chicago LSJ pulls out the section of χράω cited in that cross-reference into its own entry.

I am content to leave these 57 in the Chicago data as they are. Before about a fortnight ago, no one was using CITE2 URNs to refer to any *LSJ* entries (as far as I know), so having URNs based on the Chicago data does not bother me. (And anyway, the original [Perseus data remains online for querying](https://eumaeus.github.io/2018/11/05/chicago.html).)

Which leaves the 419 entries in Perseus but not in Chicago. I've made the editorial decision to include these in the Chicago data, with their ID-values from Perseus, as a set of entries as the end of the ordered collection. I have *not* done the work to insert them in the sequence. A search will find them, and they will appear in the alphabetic lists (but at the at the end of those lists). All that is required is to get that list, extract it from the Perseus data, and change the URNs.

~~~
// Keep the Perseus hapaxes that have no counterpart in Chicago data 
val perseusKeepersIds:Vector[String] = {
	val unwanted = hapaxMatches.map(_._1._1.objectComponent.toString).toSet
	perseusHapax.filterNot(unwanted)
}

// Get those lines, rewrite URNs
val perseusKeepers:Vector[String] = {
	perseusKeepersIds.map(pki => {
		val matchedString = persStr.filter(_.split("#")(1) == s"${perseusUrnBase}${pki}")(0)
		val newLine = matchedString.replace(perseusUrnBase, chicagoUrnBase)
		newLine
	})
}

// write these to a file
val supplementFile:String = "chicago/Chicago-markdown-lsj/cex-supplement.cex"
val pw = new PrintWriter(new java.io.File(supplementFile))
pw.write("")
for (l <- perseusKeepers) {
	val splitted = l.split("#")
	val thisLine = s"${splitted(1)}#${splitted(2)}#${splitted(3)}\n" // drop the initial sequence number
	pw.append(thisLine)
}
pw.close
~~~ 

So now we have **116,854** entries in our *LSJ* dataset, based on the work the the University of Chicago team did with the data that the Perseus digital library provided.

I am content, for the moment, with this data. It is [freely available for anyone to inspect, repurpose, and improve](https://github.com/Eumaeus/cite_lsj_cex), and I would love to see the results of this work. The work I've described here is based on decades of ongoing thought and labor by scholars in Medford, Chicago, and Leipzig, for which I am very grateful. 