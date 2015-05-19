# NotaQL

NotaQL is a cross-system transformation language. It provides a concise way to
describe data transformations between different stores mainly from the NoSQL domain.
The supported technologies are currently MongoDB, HBase, Redis, and CSV files.
It is built to be extensible, meaning that additional store support can easily be added.
With scalability being one central part, NotaQL transformations can be executed
on small to big data.

<http://notaql.github.io/>

## Samples

### Page Impressions Counter

In real systems it is often necessary to extract hot data from your main database into a faster one.
This is where key-value stores have their main benefits when compared to stores with more complex data models.

If for example our product catalog is stored in HBase, we might want to track page impressions in Redis.
At some point however you want to transfer the current state of the page impression counter back to HBase.
The reason for that could be that this grants us to only have one database hit instead of two when loading a product page.

#### Redis input

| Key   | Value |
|-------|-------|
| plate | 1100 |
| table | 25000  |

#### MongoDB output

```JSON
{
  "_id": "plate",
  "price": 4,
  "info": {
    "company": "WFM",
    "views": 1100
  }
}
{
  "_id": "table",
  "price": 480,
  "info": {
    "company": "Eikia",
    "views": 25000
  }
}
```

#### Transformation

```
OUT._id <- IN._k,
OUT.info.company <- IN._v
```

### Aggregation

Other tasks may include aggregating data. For example we might be interested in the average number of views of products of the companies.
Let's say the result shall be stored in HBase.

#### MongoDB input

```JSON
{
  "_id": "plate",
  "price": 4,
  "info": {
    "company": "WFM",
    "views": 1100
  }
}
{
  "_id": "knife",
  "price": 20,
  "info": {
    "company": "Eikia",
    "views": 15000
  }
}
{
  "_id": "table",
  "price": 480,
  "info": {
    "company": "Eikia",
    "views": 25000
  }
}
```

#### HBase output

| row | avg_views |
|-----|-------|
| WFM | 1100  |
| Eikia | 20000 |


#### Transformation

```
OUT._r <- IN.info.company,
OUT.avg_views <- AVG(IN.info.views)
```

There are many more example where NotaQL may be used. If you run into issues do not hesitate to contact us.

### More Examples

More examples can be found [here](EXAMPLES.md).

## Community

Please use our IRC or mailing list for getting in touch on development and support.

- #notaql on irc.freenode.net
- [notaql@googlegroups.com](https://groups.google.com/d/forum/notaql)

## News

### May 18th 2015

This is the initial publication of the NotaQL framework for cross-system data transformations.

## About

This project is a research prototype created at the
[Heterogeneous Information Systems Group](http://wwwlgis.informatik.uni-kl.de/cms/his/) at the Technical University of Kaiserslautern.
The framework was written by [TomLottermann](https://github.com/TomLottermann) during his work for his master's thesis with [jschildgen](https://github.com/jschildgen) being his supervisor.
We open sourced it in order to allow new contributors to add new features, bug fixes, or simply
use it for their own purposes.
