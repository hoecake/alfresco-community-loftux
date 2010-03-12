/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

(function() 
{
   var Dom = YAHOO.util.Dom,
       Event = YAHOO.util.Event,
       KeyListener = YAHOO.util.KeyListener,
       Selector = YAHOO.util.Selector,
       Element = YAHOO.util.Element,
       Bubbling = YAHOO.Bubbling,
       Cookie = YAHOO.util.Cookie,
       WebEditor = YAHOO.org.springframework.extensions.webeditor;

   YAHOO.namespace('org.springframework.extensions.webeditor.ui.tabbed-toolbar');

   WebEditor.ui.TabbedToolbar = function YAHOO_org_springframework_extensions_webeditor_ui_TabbedToolbar_constructor(config)
   {
      WebEditor.ui.TabbedToolbar.superclass.constructor.apply(this, Array.prototype.slice.call(arguments));
   };

   YAHOO.extend(WebEditor.ui.TabbedToolbar, WEF.Widget,
   {
      init: function WEF_UI_TabbedToolbar_init()
      {
         WebEditor.ui.TabbedToolbar.superclass.init.apply(this);

         this.initAttributes(this.config);
         var d = document.createElement('div');
         d.className = 'wef-tab-toolbar';
         this.element.get('element').appendChild(d);
         this.element = new Element(d);
         //create tabview
         this.widgets.tabview = new YAHOO.widget.TabView();
         
         this.widgets.tabview.appendTo(this.element);
         this.widgets.tabview.on('activeTabChange', this.onTabChange ,this, true);
         
         this.widgets.toolbars = [];

         if (this.config.toolbars)
         {
            for (var i = 0, len = this.config.toolbars.length; i < len; i++)
            {
               var tbConfig = this.config.toolbars[i];
               this.addToolbar(tbConfig.id, tbConfig);
            }
         }
      },

      render: function WEF_UI_TabbedToolbar_render()
      {
         var toolbars = this.widgets.toolbars;
         for (var i = 0, len = this.widgets.toolbars.length; i < len ;i++)
         {
            toolbars[i].init();
         }
      },

      addToolbar: function WEF_UI_TabbedToolbar_addToolbar(id, config)
      {
         var toolbar, 
            tab  = new YAHOO.widget.Tab(
            {
               label: config.label,
               content: config.content || "",
               active: config.active  || false
            });
            
         tab.pluginOwner = config.pluginOwner || null;
         this.widgets.tabview.addTab(tab);
         
         var toolbarConfig = 
         {
            id:config.id+'-toolbar',
            name:config.id+'-toolbar',
            element: tab.get('contentEl'),
            buttons: { buttons: config.buttons || []}
         };

         
         toolbar = new WebEditor.ui.Toolbar(toolbarConfig);
         this.widgets.toolbars.push(toolbar);
         this.widgets.toolbars[id] = toolbar;
         if (this.widgets.toolbars.length>0)
         {
            this.widgets.tabview.set('activeTab',this.widgets.tabview.getTab(0));
            this.show();
         }
         return toolbar;
      },

      addButtons : function WEF_UI_TabbedToolbar_addButtons(toolbarId, buttonConfig)
      {
         var toolbars  = this.widgets.toolbars;
         if (toolbars[toolbarId])
         {
            toolbars[toolbarId].addButtons(buttonConfig);
         }
      },

      getToolbar: function WEF_UI_TabbedToolbar_getToolbar(toolbarId)
      {
         return this.widgets.toolbars[toolbarId];
      },
      
      onTabChange: function WEF_UI_TabbedToolbar_onTabChange(e)
      {
         Bubbling.fire(this.config.name + WEF.SEPARATOR + 'tabChange', e);

      }
   });
})();

WEF.register("org.springframework.extensions.webeditor.ui.tabbed-toolbar", YAHOO.org.springframework.extensions.webeditor.ui.TabbedToolbar, {version: "1.0", build: "1"});