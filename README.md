<h1 align="center">Rapid Automatic Keyword Extraction (RAKE)</h1>

<p align="center">
    <img src="https://images-na.ssl-images-amazon.com/images/I/51jh-BmYlgL._SL1000_.jpg" alt="Rake" width="300" />
<p align="center">

RAKE is an algorithm for extracting *keywords* (technically phrases, but I don't question scientific literature) from a
document that have a high relevance or importance to the contents of the document. For example, the top five significant keywords
in the text:

> Compatibility of systems of linear constraints over the set of natural numbers. Criteria of compatibility of a system of linear Diophantine equations, strict inequations, and nonstrict inequations are considered. Upper bounds for components of a minimal set of solutions and algorithms of construction of minimal generating sets of solutions for all types of systems are given. These criteria and the corresponding algorithms for constructing a minimal supporting set of solutions can be used in solving all the considered types of systems and systems of mixed types.

are calculated to be:

Keyword                       | Relevance
------------------------------|----------:
linear diophantine equations  | 10.666
minimal generating sets       | 10.333
minimal supporting set        | 8.833
upper bounds                  | 6.0
natural numbers               | 6.0

Installing
----------
This library isn't on a central repo yet, so nab the JAR URL from the releases page and toss it into whatever dependency
manager you're using. That, or just download the JAR.

Using the API
-------------
Using the library is super straightforward. The main class only exports one public method, `getKeywordsFromText()`, and 
needs a language code in order to run. Any constant found in `RakeLanguages` can be used without issue. So for example:

```java
public class Main {
    public static void main(String[] args) {
        String languageCode = RakeLanguages.ENGLISH;
        Rake rake = new Rake(languageCode);
        LinkedHashMap<String, Double> results = rake.getKeywordsFromText("Your text would go here."));
    	System.out.println(results);
    }
}
```

Contributing
------------
If you wish to contribute to this library, you'll need [Buck](http://buckbuild.com) to build it. Once you have Buck 
installed, it's as simple as:

```bash
$ buck build :rake
``` 

Credit
------
Algorithm taken from [here](https://www.researchgate.net/publication/227988510_Automatic_Keyword_Extraction_from_Individual_Documents).

Implementation taken from [here](https://github.com/aneesha/RAKE).
