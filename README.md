# Datomic-ish

A Clojure experiment to simulate datomic over PostgreSQL

## Usage

Create a datom-like structure:

```sql
CREATE TABLE txt_datoms(
  id UUID,
  attribute VARCHAR,
  value VARCHAR,
  retract boolean,
  txed_at tstzrange,
  valid_at tstzrange
);

CREATE INDEX ON txt_datoms(valid_at);
CREATE INDEX ON txt_datoms  USING HASH (attribute);
CREATE INDEX ON txt_datoms(value) ;
CREATE INDEX ON txt_datoms(id);
```

Profit

## License

Copyright © 2021 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
