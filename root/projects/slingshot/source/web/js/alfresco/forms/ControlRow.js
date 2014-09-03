/**
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

/**
 * This module provides a way in which [form controls]{@link module:alfresco/forms/controls/BaseFormControl}
 * can be horizontally aligned within a [form]{@link module:alfresco/forms/Form}. It extends the
 * [horizontal widgets layout widget]{@link module:alfresco/layout/HorizontalWidgets} to gain the layout, dimensions
 * and resizing capabilities and aliases the expected functions to iterate over all the
 * [form controls]{@link module:alfresco/forms/controls/BaseFormControl} that it may have processed.
 * 
 * @module alfresco/forms/ControlRow
 * @extends module:alfresco/layout/HorizontalWidgets
 * @author Dave Draper
 */
define(["alfresco/layout/HorizontalWidgets",
        "dojo/_base/declare",
        "dojo/_base/lang",
        "dojo/_base/array"], 
        function(HorizontalWidgets, declare, lang, array) {
   
   return declare([HorizontalWidgets], {
      
      /**
       * Iterates over the array of processed widgets and adds the value of each to the supplied object
       *
       * @instance
       * @param {object} values The object to set with the values from each form control
       */
      addFormControlValue: function alfresco_forms_ControlRow__addFormControlValue(values) {
         array.forEach(this._processedWidgets, lang.hitch(this, this.addChildFormControlValue, values));
      },

      /**
       * 
       * @instance
       * @param {object} values The object to set with the value of the supplied widget
       * @param {object} widget The widget to get the value from
       * @param {number} index The index of the widget
       */
      addChildFormControlValue: function alfresco_forms_ControlRow__addChildFormControlValue(values, widget, index) {
         if (typeof widget.addFormControlValue === "function")
         {
            widget.addFormControlValue(values);
         }
      },

      /**
       * 
       * @instance
       * @param {object} values The object to set the each form control value from
       */
      updateFormControlValue: function alfresco_forms_ControlRow__addFormControlValue(values) {
         array.forEach(this._processedWidgets, lang.hitch(this, this.updateChildFormControlValue, values));
      },

      /**
       * 
       * @instance
       * @param {object} values The object to set with the value of the supplied widget
       * @param {object} widget The widget to get the value from
       * @param {number} index The index of the widget
       */
      updateChildFormControlValue: function alfresco_forms_ControlRow__updateChildFormControlValue(values, widget, index) {
         if (typeof widget.addFormControlValue === "function")
         {
            widget.addFormControlValue(values);
         }
      },

      /**
       * Iterates over the child form controls and validates each one.
       * 
       * @instance
       */
      validateFormControl: function alfresco_forms_ControlRow__validateFormControl() {
         array.forEach(this._processedWidgets, lang.hitch(this, this.validateChildFormControlValue));
      },

      /**
       *
       * @instance
       * @param {object} widget The widget to validate
       * @param {number} index The index of the widget to validate
       */
      validateChildFormControlValue: function alfresco_forms_ControlRow__validateChildFormControlValue(widget, index) {
         if (typeof widget.validateChildFormControlValue === "function")
         {
            widget.validateChildFormControlValue(values);
         }
      }
   });
});