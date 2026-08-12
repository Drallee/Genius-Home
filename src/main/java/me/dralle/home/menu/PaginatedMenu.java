package me.dralle.home.menu;

import me.dralle.home.HomePlugin;
import me.dralle.home.models.Home;
import me.dralle.home.utils.HomeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static me.dralle.home.utils.Utils.rep;

public abstract class PaginatedMenu extends Menu {
    protected int page = 0;
    protected long pages = 1;
    protected int indexes = 0;

    public PaginatedMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    public void getIndexes() {
        List<Home> homes = playerMenuUtility.getPlayerHomes();
        long pageSize = getMaxItemsPerPage();
        pages = HomeUtils.calculatePagesCount(pageSize, homes.size());
        indexes = calculateLastIndex(homes.size());
    }

    public void getIconsIndexes() {
        ArrayList<String> icons = HomeUtils.getHomeIcons();
        long pageSize = getMaxItemsPerPage();
        pages = HomeUtils.calculatePagesCount(pageSize, icons.size());
        indexes = calculateLastIndex(icons.size());
    }

    public void getHeadsIndexes() {
        ArrayList<String> icons = HomeUtils.getHomeIconsPlayerHeads();
        long pageSize = getMaxItemsPerPage();
        pages = HomeUtils.calculatePagesCount(pageSize, icons.size());
        indexes = calculateLastIndex(icons.size());
    }

    public void getSoundsIndexes() {
        List<Map<?, ?>> sounds = HomePlugin.getSoundsConfig().getMapList("sounds.list");
        long pageSize = getMaxItemsPerPage();
        pages = HomeUtils.calculatePagesCount(pageSize, sounds.size());
        indexes = calculateLastIndex(sounds.size());
    }

    protected int calculateLastIndex(int totalSize) {
        int last = getMaxItemsPerPage() * page;
        if (totalSize <= 0) return last;
        for (int i = 0; i < getMaxItemsPerPage(); i++) {
            int current = getMaxItemsPerPage() * page + i;
            if (current >= totalSize) break;
            last = current;
        }
        return last;
    }

    protected void addMenuBorderDefault() {
        getIndexes();
        addPaginatedControls(playerMenuUtility.getPlayerHomes().size(), "close");
        setFillerGlass();
    }

    protected void addMenuBorderHomeIcons() {
        getIconsIndexes();
        addPaginatedControls(HomeUtils.getHomeIcons().size(), "close");
        setFillerGlass();
    }

    protected void addMenuBorderPlayerHeads() {
        getHeadsIndexes();
        addPaginatedControls(HomeUtils.getHomeIconsPlayerHeads().size(), "close");
        setFillerGlass();
    }

    protected void addMenuBorderSounds() {
        getSoundsIndexes();
        addPaginatedControls(HomePlugin.getSoundsConfig().getMapList("sounds.list").size(), "close");
        setFillerGlass();
    }

    protected void addPaginatedControls(int totalSize, String closeAction) {
        if (page != 0) {
            setButton("previous-page", "previous-page", "%current%", page + 1, "%total%", pages);
        }
        if ((indexes + 1) < totalSize) {
            setButton("next-page", "next-page", "%current%", page + 1, "%total%", pages);
        }
        setButton("close", closeAction);
    }

    protected Integer getClickedContentIndex(int rawSlot) {
        List<Integer> contentSlots = getContentSlots();
        int indexInPage = contentSlots.indexOf(rawSlot);
        if (indexInPage < 0) {
            return null;
        }
        return page * getMaxItemsPerPage() + indexInPage;
    }

    public int getMaxItemsPerPage() {
        return getContentSlots().size();
    }
}
