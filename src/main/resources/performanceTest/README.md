Test 1
======

Test 1 selects 50% of the items and then performs aggregation based on them

Aggregation Pipeline Performance
--------------------------------

We should compare this to the same executed directly by the MongoDB Aggregation pipeline.

The query for this is the following:

```javascript
db.nobench_10_newreduced.aggregate(
	[
		{ $match: { bool: true }},
		{ $group: { _id: "$thousandth", count: { $sum: 1 } } },
		{ $out: "nobench_10_reducedoutNativeMongoDB" }
	]
)
```


Test 2
======

Test 2 selects 1/1000th of the entries and then performs aggregations on them

MongoDB native
--------------

We should compare this to the same executed directly by MongoDB.

The query for this is the following:

```javascript
db.nobench_10_newreduced.aggregate(
	[
		{ $match: { sparse_601: "GBRDCMJQ" }},
		{ $out: "nobench_10_reducedout2NativeMongoDB" }
	]
)
```
