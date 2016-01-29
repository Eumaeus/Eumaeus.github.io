// A silly script for demonstrating now CTS URNs, with subreferences, 
// can bring order to chaos by turning content-objects into
// cittion-objects.
// ©2016 Christopher W. Blackwell & Neel Smith, The Homer Multitext. 
// Licensed under the GPL:
// http://www.gnu.org/licenses/gpl-3.0.en.html


// All libs available from beta.hpcc.uh.edu:

@GrabResolver(name='beta', root='http://beta.hpcc.uh.edu/nexus/content/repositories/releases')
@Grab(group='edu.harvard.chs', module='cite', version='1.0.4')

// necessary imports

import edu.harvard.chs.cite.CtsUrn
import org.junit.Test
import static groovy.test.GroovyAssert.shouldFail

println ""
println ""

String test1 = "οὐλομένην" 
String test2 = "οὐλομένην"
String test3 = "οὐλομένην"

String urnString1 = "urn:cts:greekLit:tlg0012.tlg001.anon1:1.2@" + test1
String urnString2 = "urn:cts:greekLit:tlg0012.tlg001.anon2:1.2@" + test2
String urnString3 = "urn:cts:greekLit:tlg0012.tlg001.anon3:1.2@" + test3

println ""
println "The Simplest Possible 'Homer Multitext'"
println "---------------------------------------"
println ""

println "Iliad edition anon1 begins with: ${test1}"
println "Iliad edition anon2 begins with: ${test2}"
println "Iliad edition anon3 begins with: ${test3}"
println ""

println "---------------------------------------"
println "Let's Analyze our Homeric Tradition!"
println "---------------------------------------"
println ""

println "Does anon1 begin with the same word as anon2?"
println "${test1} == ${test2} : ${test1 == test2}"
println "Does anon1 begin with the same word as anon3?"
println "${test1} == ${test3} : ${test1 == test3}"
println "Does anon3 begin with the same word as anon2?"
println "${test3} == ${test2} : ${test3 == test2}"

shouldFail {
	assert ${test1} == ${test2}
	assert ${test1} == ${test3}
	assert ${test3} == ${test2}
}

println ""
println "---------------------------------------"
println "Using an Infrastructure for Digital Scholarly Editions"
println "we can make our data subject: reproducible, valid, and verified."
println "---------------------------------------"
println ""

println "A CTS citation to anon1: urnString1 = ${urnString1}"
println "A CTS citation to anon2: urnString2 = ${urnString2}"
println "A CTS citation to anon3: urnString3 = ${urnString3}"

println ""
println "---------------------------------------"
println "We use the CITE Library to create"
println "CTS-URN objects, normalized according to a scholarly understanding of Greek."
println "---------------------------------------"
println ""

println "CtsUrn urn1 = new CtsUrn(urnString1)"
println "CtsUrn urn2 = new CtsUrn(urnString2)"
println "CtsUrn urn3 = new CtsUrn(urnString3)"

CtsUrn urn1 = new CtsUrn(urnString1)
CtsUrn urn2 = new CtsUrn(urnString2)
CtsUrn urn3 = new CtsUrn(urnString3)

println ""
println "---------------------------------------"
println "We can access the now-citable subreferences with:"
println "urn1.subref, urn2.subref, urn3.subref"
println "---------------------------------------"
println ""

println "urn1.subref : ${urn1.subref}"
println "urn2.subref : ${urn2.subref}"
println "urn3.subref : ${urn3.subref}"

println ""
println "And we can confirm that these three texts are the same."
println ""

assert urn1.subref == urn2.subref
assert urn1.subref == urn3.subref
assert urn3.subref == urn2.subref

println "urn1.subref == urn2.subref : ${urn1.subref == urn2.subref}"
println "urn1.subref == urn3.subref : ${urn1.subref == urn3.subref}"
println "urn3.subref == urn2.subref : ${urn3.subref == urn2.subref}"

println ""
println "This stuff doesn't just happen! Thanks, Neel, for obsessing on Unicode!"
println ""







