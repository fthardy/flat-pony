# flat-pony
_flat-pony_ provides a framework for reading and writing so-called flat data formats. _flat-pony_ is suitable for
processing both fixed length and delimited formats. The focus is on the processing of nested data structures which in
particular are common with fixed length formats. Hence, processing of tabular data structures, typical for delimited
formats like CSV is possible with _flat-pony_, but not highly recommended.

##### Why yet another framework for processing flat data?
Well, I know that there are already a number of frameworks for processing flat data out there, such as Flatworm, Flatpak
or Bean-IO. However, all of these frameworks/APIs have their reason to exist, but they have, like everything in our
universe, their weaken and strengthen.

Currently I have to use Flatworm in a project. But over time it turned out that Flatworm is not well suited to our
application scenario. After a (admittedly not profound) evaluation of several other options I came to the conclusion
that none of these frameworks/APIs meet our requirements.

As a result from the experiences with using Flatworm, the idea for this framework was formed. And in the end, I came
to the conclusion that I should implement this idea - knowing that I was going to reinvent the wheel. But hey, I'm a
passionate software engineer, I'm pretty convinced of my approach and I burn my private time and money, not that of my
company! So what? ;~D

##### Why or where does the name flat-pony come from?
Well, it just occurred to me, I thought it was funny and I was tired of searching for a suitable, memorizable name.

##### Framework design
The design of the framework is heavily based upon GoF-patterns like composite, visitor, decorator etc. Extensibility
is based on sub typing through interfaces and decorators and object composition. Implementations are kept immutable as
far as possible. Making implementations final should prevent from direct sub classing.

For creating data structure definitions a fluent API is favored in stead of a configuration DSL. However, it should be
possible to build any configuration DSL upon the fluent API. 

The framework doesn't support binding of data to beans. However, it creates a generic data model which is used to modify
a data structure and write it back into a character stream. This approach is comparable with the XML DOM-Parser.
However, parsing and writing flat data in a push- and a pull-fashion is planed as well.    

### Current state of the project
Actually the core of framework is available but still in development. Some things might change and I'm not yet happy
with the current state of code coverage. But, I'm looking forward to release the first stable version soon.
So hang on... 
