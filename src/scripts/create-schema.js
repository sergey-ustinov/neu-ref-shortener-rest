use neueda

db.reference.drop();
db.createCollection("reference");
db.reference.createIndex({ "base10Ref": 1 }, { unique: true, name: "reference_base10Ref_uidx" });
db.reference.createIndex({ "base62Ref": 1 }, { unique: true, name: "reference_base62Ref_uidx" });
db.reference.createIndex({ "created": 1 }, { name: "reference_created_uidx" });

db.sequence.drop();
db.createCollection("sequence");
db.sequence.createIndex({ "name": 1 }, { unique: true, name: "sequence_name_uidx" });
db.sequence.insertOne({ "name": "default", "value": NumberLong(0) });