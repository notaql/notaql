# Examples

In this document we intent to demonstrate all the different use cases for NotaQL.
For the sake of simplicity, most of the transformations shown here stay in the same stores.
However, the same techniques can be applied to cross-system transformations as well.

## HBase

### Basic Transformations

#### Projection

In this sample, we see a simple projection that provides the price and company for each input row (i.e. product).

##### Input

| row-id | info  |         |        |
|--------|-------|---------|--------|
|        | price | company | weight |
| plate  | 4     | WFM     | 110    |
|        | price | company | weight |
| knife  | 20    | Eikia   | 65     |

##### Output

| row-id |       |         |
|--------|-------|---------|
|        | price | company |
| plate  | 4     | WFM     |
|        | price | company |
| knife  | 20    | Eikia   |

##### Transformation

```
IN-ENGINE: hbase(table_id <- 'products'), # source
OUT-ENGINE: hbase(table_id <- 'projected_products'), # target
OUT._r <- IN._r, # the row ids are the same in the output
OUT.price <- IN.price, # create a column with the name "price" which contains the value of the price in the input
OUT.company <- IN.company # similar as above
```

#### Resolved Column Names

As often in NoSQL stores, the structure of the input might not be known well enough.
The reason for this is that column names are often used for storing data and not just metadata.

The following sample features a dynamic number of columns that define ratings and the column names reference users.
If we, for example, want to simply copy an unknown input table, we need some easy way to access the data and metadata of cells dynamically.

##### Input

| row-id | ratings |         |
|--------|---------|---------|
|        | Arthur  | Tricia  |
| plate  | 5       | 4       |
|        | Marvin  | Tricia  |
| knife  | 4       | 3       |

##### Output

| row-id | ratings |         |
|--------|---------|---------|
|        | Arthur  | Tricia  |
| plate  | 5       | 4       |
|        | Marvin  | Tricia  |
| knife  | 4       | 3       |

##### Transformation

```
IN-ENGINE: hbase(table_id <- 'ratings'), # source
OUT-ENGINE: hbase(table_id <- 'copied_ratings'), # target
OUT._r <- IN._r, # the row ids are the same in the output
OUT.$(IN._c) <- IN._v # the column names are the same as before and the values stored inside of them are as well
```

Here we can also see one strength of NotaQL. It allows to dynamically resolve column names based on data in the input.
This is very difficult to achieve using Hive or other SQL like languages.

#### Input Filters

It is sometimes necessary to exclude some items from the transformation. For that purpose we propose so-called input filters.

In the following example we only consider items manufactured by "Eikia".

##### Input

| row-id | info  |         |        |
|--------|-------|---------|--------|
|        | price | company | weight |
| plate  | 4     | WFM     | 110    |
|        | price | company | weight |
| knife  | 20    | Eikia   | 65     |

##### Output

| row-id |       |         |
|--------|-------|---------|
|        | price | company |
| plate  | 4     | WFM     |
|        | price | company |
| knife  | 20    | Eikia   |

##### Transformation

```
IN-ENGINE: hbase(table_id <- 'products'), # source
OUT-ENGINE: hbase(table_id <- 'eikia_products'), # target
IN-FILTER: IN.company = 'Eikia',
OUT._r <- IN._r, # the row ids are the same in the output
OUT.price <- IN.price, # create a column with the name "price" which contains the value of the price in the input
```

#### Cell Filters

Sometimes it might also be necessary to include only certain columns in a transformation.
For that purpose we have so-called cell filters.

In the following example we include only the cells, where the addressed rating is above 3 (i.e. resulting products only include the ratings above 3).
The expression `IN._c?(@ > 3)` can be read as: all the names of the columns, where the value of this cell (i.e. `@`) is greater than 3.

##### Input

| row-id | ratings |         |
|--------|---------|---------|
|        | Arthur  | Tricia  |
| plate  | 5       | 4       |
|        | Marvin  | Tricia  |
| knife  | 4       | 3       |

##### Output

| row-id | ratings |         |
|--------|---------|---------|
|        | Arthur  | Tricia  |
| plate  | 5       | 4       |
|        | Marvin  |         |
| knife  | 4       |         |

##### Transformation

```
IN-ENGINE: hbase(table_id <- 'products'), # source
OUT-ENGINE: hbase(table_id <- 'good_reviews'), # target
OUT._r <- IN._r,
OUT.$(IN._c?(@ > 3)) <- IN._v
```

### Aggregation

Up until now we only considered one-to-one transformations. Often it is also important that data gets aggregated.
Luckily, NotaQL was built to allow just that in a concise way.

#### Projection

In this sample, we see a simple projection that provides the price and company for each input row (i.e. product).

##### Input

| row-id | info  |         |
|--------|-------|---------|
|        | price | company |
| plate  | 4     | WFM     |
|        | price | company |
| knife  | 20    | Eikia   |
|        | price | company |
| table  | 480   | Eikia   |

##### Output

| row-id |           |
|--------|-----------|
|        | avg_price |
| WFM    | 4         |
|        | avg_price |
| Eikia  | 250       |

##### Transformation

```
IN-ENGINE: hbase(table_id <- 'products'), # source
OUT-ENGINE: hbase(table_id <- 'company_averages'), # target
OUT._r <- IN.company, # the row ids are the companies of the input
OUT.avg_price <- AVG(IN.price) # take the average of the prices over the groups of companies
```

Here we can see a very handy property of NotaQL. In HBase it forms groups over the output row id by default.
This way it is very easy to perform aggregations.

## MongoDB

_WIP_

## Redis

_WIP_

## CSV

_WIP_