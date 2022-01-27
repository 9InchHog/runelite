package net.runelite.client.util;

import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import javax.inject.Inject;

public class LocationUtils {
    @Inject
    private Client client;

    /**
     * Location utils to support J plugins
     */
    public String getSpaceFixedPlayerName(String name) {
        return name.replace(" ", " ");
    }

    public boolean actorWithinTemplateBounds(Actor actor, int x1, int x2, int y1, int y2) {
        if (actor == null)
            return false;
        WorldPoint point = actorTemplateWorldLocation(actor);
        return (point.getX() >= x1 && point.getX() <= x2 && point.getY() >= y1 && point.getY() <= y2);
    }

    public boolean withinTemplateBounds(int x1, int x2, int y1, int y2) {
        if (client.getLocalPlayer() == null)
            return false;
        if (!client.isInInstancedRegion())
            return false;
        WorldPoint point = templateWorldLocation();
        return (point.getX() >= x1 && point.getX() <= x2 && point.getY() >= y1 && point.getY() <= y2);
    }

    public boolean inTemplateWorldPoint(int x, int y) {
        if (client.getLocalPlayer() == null)
            return false;
        WorldPoint point = templateWorldLocation();
        if (point == null)
            return false;
        return (point.getX() == x && point.getY() == y);
    }

    public boolean inWorldPoint(int x, int y) {
        if (client.getLocalPlayer() == null)
            return false;
        WorldPoint point = client.getLocalPlayer().getWorldLocation();
        if (point == null)
            return false;
        return (point.getX() == x && point.getY() == y);
    }

    public boolean withinBounds(int x1, int x2, int y1, int y2) {
        if (client.getLocalPlayer() == null)
            return false;
        if (client.getLocalPlayer().getLocalLocation() == null)
            return false;
        WorldPoint point = client.getLocalPlayer().getWorldLocation();
        return (point.getX() >= x1 && point.getX() <= x2 && point.getY() >= y1 && point.getY() <= y2);
    }

    public boolean otherWithinBounds(Player player, int x1, int x2, int y1, int y2) {
        if (player == null)
            return false;
        if (player.getLocalLocation() == null)
            return false;
        WorldPoint point = player.getWorldLocation();
        return (point.getX() >= x1 && point.getX() <= x2 && point.getY() >= y1 && point.getY() <= y2);
    }

    public WorldPoint templateWorldLocation() {
        if (client.isInInstancedRegion()) {
            int[][][] instanceTemplateChunks = client.getInstanceTemplateChunks();
            LocalPoint localPoint = LocalPoint.fromWorld(client, client.getLocalPlayer().getWorldLocation());
            int chunkData = instanceTemplateChunks[client.getPlane()][localPoint.getSceneX() / 8][localPoint.getSceneY() / 8];
            int tileX = client.getLocalPlayer().getWorldLocation().getX();
            int tileY = client.getLocalPlayer().getWorldLocation().getY();
            tileX = (chunkData >> 14 & 0x3FF) * 8 + tileX % 8;
            tileY = (chunkData >> 3 & 0x7FF) * 8 + tileY % 8;
            return new WorldPoint(tileX, tileY, client.getPlane());
        }
        return null;
    }

    public WorldPoint actorTemplateWorldLocation(Actor actor) {
        if (client.isInInstancedRegion()) {
            int[][][] instanceTemplateChunks = client.getInstanceTemplateChunks();
            LocalPoint localPoint = LocalPoint.fromWorld(client, actor.getWorldLocation());
            int chunkData = instanceTemplateChunks[client.getPlane()][localPoint.getSceneX() / 8][localPoint.getSceneY() / 8];
            int tileX = actor.getWorldLocation().getX();
            int tileY = actor.getWorldLocation().getY();
            tileX = (chunkData >> 14 & 0x3FF) * 8 + tileX % 8;
            tileY = (chunkData >> 3 & 0x7FF) * 8 + tileY % 8;
            return new WorldPoint(tileX, tileY, client.getPlane());
        }
        return null;
    }
}
