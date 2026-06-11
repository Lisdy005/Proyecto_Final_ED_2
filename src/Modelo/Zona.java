package Modelo;

public enum Zona {
    AVENTURA,
    INFANTIL,
    ACUATICA,
    TERROR,
    COMIDA;

    public static Zona fromInt(int opcion) {
        switch (opcion) {
            case 1: return AVENTURA;
            case 2: return INFANTIL;
            case 3: return ACUATICA;
            case 4: return TERROR;
            case 5: return COMIDA;
            default: return null;
        }
    }

    public static void mostrarZonas() {
        Zona[] zonas = Zona.values();
        for (int i = 0; i < zonas.length; i++) {
            System.out.println((i+1) + ". " + zonas[i].name());
        }
    }
}