package hoten.gridia.map;

import com.google.gson.Gson;
import hoten.gridia.Creature;
import hoten.gridia.content.ItemInstance;

public class TileMap {

    public final int size, depth, sectorSize, area, volume, sectorsAcross, sectorsFloor, sectorsTotal;
    private final Sector[][][] _sectors;
    private final SectorLoader _sectorLoader;
    private final SectorSaver _sectorSaver;

    public TileMap(int size, int depth, int sectorSize, SectorLoader sectorLoader, SectorSaver sectorSaver) {
        if (size % sectorSize != 0) {
            throw new IllegalArgumentException("sectorSize must be a factor of size");
        }
        this.size = size;
        this.depth = depth;
        this.sectorSize = sectorSize;
        area = size * size;
        volume = area * depth;
        sectorsAcross = size / sectorSize;
        sectorsFloor = sectorsAcross * sectorsAcross;
        sectorsTotal = sectorsFloor * depth;
        _sectors = new Sector[sectorsAcross][sectorsAcross][depth];
        _sectorLoader = sectorLoader;
        _sectorSaver = sectorSaver;
    }

    public TileMap(int size, int depth, int sectorSize) {
        this(size, depth, sectorSize, new JsonSectorLoader(), new SectorSaver());
    }

    //temporary
    public void loadAll() {
        for (int x = 0; x < sectorsAcross; x++) {
            for (int y = 0; y < sectorsAcross; y++) {
                for (int z = 0; z < depth; z++) {
                    Sector sector = _sectors[x][y][z];
                    if (sector != null) {
                        _sectors[x][y][z] = _sectorLoader.load(sectorSize, x, y, z);
                    }
                }
            }
        }
    }

    public void save() {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < depth; z++) {
                    Sector sector = _sectors[x][y][z];
                    if (sector != null) {
                        _sectorSaver.save(sector);
                    }
                }
            }
        }
    }

    public String getSectorData(int sx, int sy, int sz) {
        Sector sector = getSector(sx, sy, sz);
        Gson gson = new Gson(); // :(
        return gson.toJson(sector._tiles);
    }

    public Sector getSector(int sx, int sy, int sz) {
        Sector sector = _sectors[sx][sy][sz];
        if (sector == null) {
            _sectors[sx][sy][sz] = sector = _sectorLoader.load(sectorSize, sx, sy, sz);
        }
        return sector;
    }

    public Sector getSectorOf(Coord loc) {
        return getSectorOf(loc.x, loc.y, loc.z);
    }

    public Sector getSectorOf(int x, int y, int z) {
        x = wrap(x);
        y = wrap(y);
        int sx = x / sectorSize;
        int sy = y / sectorSize;
        return getSector(sx, sy, z);
    }

    public Tile getTile(Coord loc) {
        return getTile(loc.x, loc.y, loc.z);
    }

    public Tile getTile(int x, int y, int z) {
        x = wrap(x);
        y = wrap(y);
        return getSectorOf(x, y, z).getTile(x % sectorSize, y % sectorSize);
    }

    public int getFloor(int x, int y, int z) {
        return getTile(x, y, z).floor;
    }

    public void setFloor(int x, int y, int z, int floor) {
        getTile(x, y, z).floor = floor;
    }
    
    public void setFloor(Coord loc, int floor) {
        getTile(loc).floor = floor;
    }

    public ItemInstance getItem(Coord c) {
        return getTile(c.x, c.y, c.z).item;
    }

    public ItemInstance getItem(int x, int y, int z) {
        return getTile(x, y, z).item;
    }

    public void setItem(ItemInstance item, int x, int y, int z) {
        getTile(x, y, z).item = item;
    }

    public void setItem(ItemInstance item, Coord c) {
        getTile(c.x, c.y, c.z).item = item;
    }

    public Creature getCreature(Coord loc) {
        return getTile(loc).cre;
    }

    public int wrap(int value) {
        int mod = value % size;
        return mod < 0 ? size + mod : mod;
    }

    public Coord wrap(Coord loc) {
        return new Coord(wrap(loc.x), wrap(loc.y), loc.z);
    }

    public boolean walkable(int x, int y, int z) {
        Tile tile = getTile(x, y, z);
        return tile.cre == null && tile.item.data.walkable && tile.floor != 1;
    }

    public Coord getCoordFromIndex(int index) {
        int z = index / area;
        int y = (index / size) % size;
        int x = index % size;
        return new Coord(x, y, z);
    }

}