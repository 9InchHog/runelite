/*
 * Copyright (c) 2017, Aria <aria@ar1as.space>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.mousehighlight;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import java.awt.*;
import java.util.Set;
import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.api.util.Text;
import net.runelite.client.util.ColorUtil;
import org.apache.commons.lang3.StringUtils;

class MouseHighlightOverlay extends Overlay
{
	/**
	 * Menu types which are on widgets.
	 */
	private static final Set<MenuAction> WIDGET_MENU_ACTIONS = ImmutableSet.of(
		MenuAction.WIDGET_TYPE_1,
		MenuAction.WIDGET_TARGET,
		MenuAction.WIDGET_CLOSE,
		MenuAction.WIDGET_TYPE_4,
		MenuAction.WIDGET_TYPE_5,
		MenuAction.WIDGET_CONTINUE,
		MenuAction.ITEM_USE_ON_ITEM,
		MenuAction.WIDGET_USE_ON_ITEM,
		MenuAction.ITEM_FIRST_OPTION,
		MenuAction.ITEM_SECOND_OPTION,
		MenuAction.ITEM_THIRD_OPTION,
		MenuAction.ITEM_FOURTH_OPTION,
		MenuAction.ITEM_FIFTH_OPTION,
		MenuAction.ITEM_USE,
		MenuAction.WIDGET_FIRST_OPTION,
		MenuAction.WIDGET_SECOND_OPTION,
		MenuAction.WIDGET_THIRD_OPTION,
		MenuAction.WIDGET_FOURTH_OPTION,
		MenuAction.WIDGET_FIFTH_OPTION,
		MenuAction.EXAMINE_ITEM,
		MenuAction.WIDGET_TARGET_ON_WIDGET,
		MenuAction.CC_OP_LOW_PRIORITY,
		MenuAction.CC_OP
	);

	private final TooltipManager tooltipManager;
	private final Client client;
	private final MouseHighlightConfig config;

	private static final Set<MenuAction> NPC_MENU_ACTIONS = ImmutableSet.of(
			MenuAction.NPC_FIRST_OPTION,
			MenuAction.NPC_SECOND_OPTION,
			MenuAction.NPC_THIRD_OPTION,
			MenuAction.NPC_FOURTH_OPTION,
			MenuAction.NPC_FIFTH_OPTION,
			MenuAction.WIDGET_TARGET_ON_NPC,
			MenuAction.ITEM_USE_ON_NPC);

	@Inject
	MouseHighlightOverlay(Client client, TooltipManager tooltipManager, MouseHighlightConfig config)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		// additionally allow tooltips above the full screen world map and welcome screen
		drawAfterInterface(WidgetID.FULLSCREEN_CONTAINER_TLI);
		this.client = client;
		this.tooltipManager = tooltipManager;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (client.isMenuOpen())
		{
			return null;
		}

		MenuEntry[] menuEntries = client.getMenuEntries();
		int last = menuEntries.length - 1;

		if (last < 0)
		{
			return null;
		}

		MenuEntry menuEntry = menuEntries[last];
		String target = menuEntry.getTarget();
		String option = menuEntry.getOption();
		MenuAction type = menuEntry.getType();

		MenuAction menuAction = MenuAction.of(type.getId());
		String targetStripped = Text.removeLevels(target);

		if (type == MenuAction.RUNELITE_OVERLAY || type == MenuAction.CC_OP_LOW_PRIORITY)
		{
			// These are always right click only
			return null;
		}

		if (Strings.isNullOrEmpty(option))
		{
			return null;
		}

		for (NPC npcs : client.getNpcs())
		{
			if (config.removeLevels() && npcs != null && npcs.getName() != null && target.contains(npcs.getName()) && NPC_MENU_ACTIONS.contains(menuAction))
			{
				target = targetStripped;
			}
		}

		// Trivial options that don't need to be highlighted, add more as they appear.
		switch (option)
		{
			case "Walk here":
			case "Cancel":
			case "Continue":
				return null;
			case "Move":
				// Hide overlay on sliding puzzle boxes
				if (target.contains("Sliding piece"))
				{
					return null;
				}
		}

		if (WIDGET_MENU_ACTIONS.contains(type))
		{
			final int widgetId = menuEntry.getParam1();
			final int groupId = WidgetInfo.TO_GROUP(widgetId);

			if (!config.uiTooltip())
			{
				return null;
			}

			if (!config.chatboxTooltip() && groupId == WidgetInfo.CHATBOX.getGroupId())
			{
				return null;
			}

			if (config.disableSpellbooktooltip() && groupId == WidgetID.SPELLBOOK_GROUP_ID)
			{
				return null;
			}
		}

		// If this varc is set, a tooltip will be displayed soon
		int tooltipTimeout = client.getVar(VarClientInt.TOOLTIP_TIMEOUT);
		if (tooltipTimeout > client.getGameCycle())
		{
			return null;
		}

		// If this varc is set, a tooltip is already being displayed
		int tooltipDisplayed = client.getVar(VarClientInt.TOOLTIP_VISIBLE);
		if (tooltipDisplayed == 1)
		{
			return null;
		}

		tooltipManager.addFront(new Tooltip(option + (Strings.isNullOrEmpty(target) ? "" : " " + target)));
		return null;
	}
}
