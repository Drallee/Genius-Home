package me.dralle.home.menu;

import me.dralle.home.HomePlugin;
import me.dralle.home.models.Home;
import me.dralle.home.utils.HomeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static me.dralle.home.utils.Utils.*;

public abstract class PaginatedMenu extends Menu {
    protected int page = 0;
    protected long pages = 1;
    protected int indexes = 0;
    protected int maxItemsPerPage = 28;
    protected int index = 0;

    public PaginatedMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    public void getIndexes(){
        List<Home> homes = playerMenuUtility.getPlayerHomes();
        long pageSize = getMaxItemsPerPage();
        long totalCount = homes.size();
        pages = HomeUtils.calculatePagesCount(pageSize, totalCount);
        if(!homes.isEmpty()) {
            for(int i = 0; i < getMaxItemsPerPage(); i++) {
                indexes = getMaxItemsPerPage() * page + i;
                if(indexes >= homes.size()) break;
            }
        }
    }

    public void getIconsIndexes(){
        ArrayList<String> icons = HomeUtils.getHomeIcons();
        long pageSize = getMaxItemsPerPage();
        long totalCount = icons.size();
        pages = HomeUtils.calculatePagesCount(pageSize, totalCount);
        if(!icons.isEmpty()) {
            for(int i = 0; i < getMaxItemsPerPage(); i++) {
                indexes = getMaxItemsPerPage() * page + i;
                if(indexes >= icons.size()) break;
            }
        }
    }

    public void getHeadsIndexes(){
        ArrayList<String> icons = HomeUtils.getHomeIconsPlayerHeads();
        long pageSize = getMaxItemsPerPage();
        long totalCount = icons.size();
        pages = HomeUtils.calculatePagesCount(pageSize, totalCount);
        if(!icons.isEmpty()) {
            for(int i = 0; i < getMaxItemsPerPage(); i++) {
                indexes = getMaxItemsPerPage() * page + i;
                if(indexes >= icons.size()) break;
            }
        }
    }

    public void getSoundsIndexes(){
        List<Map<?, ?>> sounds = HomePlugin.getSoundsConfig().getMapList("sounds.list");
        long pageSize = getMaxItemsPerPage();
        long totalCount = sounds.size();
        pages = HomeUtils.calculatePagesCount(pageSize, totalCount);
        if(!sounds.isEmpty()) {
            for(int i = 0; i < getMaxItemsPerPage(); i++) {
                indexes = getMaxItemsPerPage() * page + i;
                if(indexes >= sounds.size()) break;
            }
        }
    }

    public void addMenuBorderDefault(){
        List<Home> homes = playerMenuUtility.getPlayerHomes();
        getIndexes();
        if(page != 0){
            String displayName = getConfigMessage("GUI.general.previous-page");
            String lore = rep(getConfigMessage("GUI.general.page-lore"), "%current%", (page + 1), "%total%", pages);
            inventory.setItem(48, createItem("ARROW", 1, displayName, lore));
        }
        if (!((indexes + 1) >= homes.size())){
            String displayName = getConfigMessage("GUI.general.next-page");
            String lore = rep(getConfigMessage("GUI.general.page-lore"), "%current%", (page + 1), "%total%", pages);
            inventory.setItem(50, createItem("ARROW", 1, displayName, lore));
        }
        inventory.setItem(49, createItem("BARRIER", 1, getConfigMessage("GUI.general.close")));
        for (int i = 0; i < 10; i++) {
            if (inventory.getItem(i) == null) inventory.setItem(i, FILLER_GLASS);
        }
        inventory.setItem(17, FILLER_GLASS);
        inventory.setItem(18, FILLER_GLASS);
        inventory.setItem(26, FILLER_GLASS);
        inventory.setItem(27, FILLER_GLASS);
        inventory.setItem(35, FILLER_GLASS);
        inventory.setItem(36, FILLER_GLASS);
        for (int i = 44; i < 54; i++) {
            if (inventory.getItem(i) == null) inventory.setItem(i, FILLER_GLASS);
        }
    }

    public void addMenuBorderHomeIcons(){
        getIconsIndexes();
        if(page != 0){
            String displayName = getConfigMessage("GUI.general.previous-page");
            String lore = rep(getConfigMessage("GUI.general.page-lore"), "%current%", (page + 1), "%total%", pages);
            inventory.setItem(48, createItem("ARROW", 1, displayName, lore));
        }
        if (!((indexes + 1) >= HomeUtils.getHomeIcons().size())){
            String displayName = getConfigMessage("GUI.general.next-page");
            String lore = rep(getConfigMessage("GUI.general.page-lore"), "%current%", (page + 1), "%total%", pages);
            inventory.setItem(50, createItem("ARROW", 1, displayName, lore));
        }
        inventory.setItem(49, createItem("BARRIER", 1, getConfigMessage("GUI.general.close")));
        for (int i = 0; i < 10; i++) {
            if (inventory.getItem(i) == null) inventory.setItem(i, FILLER_GLASS);
        }
        inventory.setItem(17, FILLER_GLASS);
        inventory.setItem(18, FILLER_GLASS);
        inventory.setItem(26, FILLER_GLASS);
        inventory.setItem(27, FILLER_GLASS);
        inventory.setItem(35, FILLER_GLASS);
        inventory.setItem(36, FILLER_GLASS);
        for (int i = 44; i < 54; i++) {
            if (inventory.getItem(i) == null) inventory.setItem(i, FILLER_GLASS);
        }
    }

    public void addMenuBorderPlayerHeads(){
        getHeadsIndexes();
        if(page != 0){
            String displayName = getConfigMessage("GUI.general.previous-page");
            String lore = rep(getConfigMessage("GUI.general.page-lore"), "%current%", (page + 1), "%total%", pages);
            inventory.setItem(48, createItem("ARROW", 1, displayName, lore));
        }
        if (!((indexes + 1) >= HomeUtils.getHomeIconsPlayerHeads().size())){
            String displayName = getConfigMessage("GUI.general.next-page");
            String lore = rep(getConfigMessage("GUI.general.page-lore"), "%current%", (page + 1), "%total%", pages);
            inventory.setItem(50, createItem("ARROW", 1, displayName, lore));
        }
        inventory.setItem(49, createItem("BARRIER", 1, getConfigMessage("GUI.general.close")));
        for (int i = 0; i < 10; i++) {
            if (inventory.getItem(i) == null) inventory.setItem(i, FILLER_GLASS);
        }
        inventory.setItem(17, FILLER_GLASS);
        inventory.setItem(18, FILLER_GLASS);
        inventory.setItem(26, FILLER_GLASS);
        inventory.setItem(27, FILLER_GLASS);
        inventory.setItem(35, FILLER_GLASS);
        inventory.setItem(36, FILLER_GLASS);
        for (int i = 44; i < 54; i++) {
            if (inventory.getItem(i) == null) inventory.setItem(i, FILLER_GLASS);
        }
    }

    public int getMaxItemsPerPage() {
        return maxItemsPerPage;
    }
}
