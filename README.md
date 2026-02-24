> https://github.com/powsybl/powsybl-benchmark is a better place for actual PowSyBl benchmarks implementation. This
> benchmark is only to support discussion about the following
> [LinkedIn post](https://www.linkedin.com/posts/kristjan-vilgo_cim-cgmes-entsoe-activity-7431581802840002560-QlUa?utm_source=social_share_send&utm_medium=member_desktop_web&rcm=ACoAABKds-EBe-Ux86-BZiu6qblrKL4Rv8Fbv1U).

Benchmark of PowSyBl CGMES features, using ENTSO-E RealGrid test configuration (CGMES 2.4.15).

Given figures are averages over 10 executions.

## PowSyBl "CGMES Model"

CGMES Model: load CIM/XML in RDF4J in-memory triple store and run SPARQL queries for generators, lines, loads.
It is a lower level internal library, used for CGMES but not only - can be used e.g. for NC profiles.

PowSyBl CGMES Model Averages:
```
Average load time: 2240.5 ms
Average lines query time: 83.4 ms
Average gens  query time: 9.1 ms
Average loads query time: 108.2 ms
```

Comparison with triplets:
- PowSyBl "CGMES Model" has similar functional perimeter as https://github.com/Haigutus/triplets, although PowSyBl CGMES Model offers complete in-memory DB engine with indexing and SPARQL querying capabilities.
- On my laptop `pandas.read_RDF("CGMES_v2.4.15_RealGridTestConfiguration_v2.zip")` takes 1.61 seconds.

```python
>>> import pandas
>>> import timeit
>>> import triplets
>>> timeit.timeit(lambda: pandas.read_RDF("CGMES_v2.4.15_RealGridTestConfiguration_v2.zip"), number=10)
16.10395559994504
```

## PowSyBl "CGMES Importer"

CGMES Importer: Load CGMES CIM/XML as a PowSyBl network ready to use for calculations.

PowSyBl "CGMES Importer" uses "CGMES Model" above to run all necessary queries **to perform conversion to PowSyBl rich network structure**.

PowSyBl CGMES Importer Averages:
```
Average load time: 3420.3 ms
Average lines query time: 0.0 ms
Average gens  query time: 0.0 ms
Average loads query time: 0.0 ms
```
(query times are all < 1 ms)

Performance comparison with triplets is not really meaningful here since functional perimeter is different.
