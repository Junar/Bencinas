package com.junar.searchbenzine;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.Entity;

;

public class SearchBenzineDaoGenerator {
    public static void main(String[] args) throws Exception {
        final int version = 100;

        Schema schema = new Schema(version, "com.junar.searchbenzine");
        schema.setDefaultJavaPackageDao("com.junar.searchbenzine.dao");
        schema.setDefaultJavaPackageTest("com.junar.searchbenzine.test");

        addPharmacy(schema);

        new DaoGenerator().generateAll(schema, "../");
    }

    private static void addPharmacy(Schema schema) {
        Entity pharmacy = schema.addEntity("Benzine");

        // fields
        pharmacy.addIdProperty();
        pharmacy.addStringProperty("name");
        pharmacy.addDoubleProperty("latitude");
        pharmacy.addDoubleProperty("longitude");
        pharmacy.addDoubleProperty("gasolina93");
        pharmacy.addDoubleProperty("gasolina95");
        pharmacy.addDoubleProperty("gasolina97");
        pharmacy.addDoubleProperty("diesel");
        pharmacy.addDoubleProperty("kerosene");
        pharmacy.addStringProperty("address");
        pharmacy.addStringProperty("schedule");

        // Example
        // "nombre": "COPEC",
        // "lat": -20.213349467685,
        // "lon": -70.148566067219,
        // "gasolina93": 878,
        // "gasolina95": 883,
        // "gasolina97": 888,
        // "kerosene": 717,
        // "diesel": 693,
        // "autoservicio": false,
        // "direccion": "VIVAR            402, Iquique",
        // "horario": "las 24 hrs. del dia"
    }
}
