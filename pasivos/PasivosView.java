/**
 * ***********************************************************************
 * Autor   : Basthian Matthews - VMetrix International Spa.
 * Fecha   : 18/12/2020.
 * Proyecto: Profuturo Pensiones Proyector - Reporte Pasivos
 * 
 * History ***************************************************************
 * Autor      :
 * Fecha      :
 * Descripcion: 
 */
package com.vmetrix.v3.custom.proyector.pasivos;

import java.time.LocalDate;

import com.vmetrix.v3.custom.commons.util.V3System;
import com.vmetrix.v3.vcode.tab.V3Tab;

import cl.vmetrix.vcube.vcode.display.IDisplayReference;
import cl.vmetrix.vcube.vcode.types.IVCodeWindow;
import cl.vmetrix.vcube.vcode.types.VCodeException;
import cl.vmetrix.vcube.vcode.types.windowCode.VLayout;
import cl.vmetrix.vcube.vcode.types.windowCode.VLayout.Orientation;
import cl.vmetrix.vcube.vcode.types.windowCode.components.VButton;
import cl.vmetrix.vcube.vcode.types.windowCode.components.VDatePicker;
import cl.vmetrix.vcube.vcode.types.windowCode.components.VGrid;
import cl.vmetrix.vcube.vcode.types.windowCode.components.VLabel;

public class PasivosView implements IVCodeWindow {	
	
	// Basic Variables
		private IDisplayReference display;
		String sReportName = "ActivosView";

//		String sDatabaseTableName = "VCUBE_USER.RISK_PROYECTOR_PASIVOS_PROCESSED";

	// Grids
		VGrid gridData				= new VGrid(sReportName);
		VDatePicker dProcessDate	= new VDatePicker("Process Date");
		
		// Main Layouts
		VLayout layoutMainV		= VLayout.getInstance( Orientation.NONE );
		VLayout layoutTop		= VLayout.getInstance( Orientation.HORIZONTAL );
		VLayout layoutContent	= VLayout.getInstance( Orientation.NONE );
		VLayout layoutDetail	= VLayout.getInstance( Orientation.VERTICAL );
	    V3Tab tabs 				= new V3Tab("Tabs");
	    
		// Components
		VLabel lblTitle				= new VLabel("Pasivos");
		VLabel lblData				= new VLabel("Data");
		VButton btnCalculate		= new VButton("Calculate");
		VButton btnSave				= new VButton("Save");
		VButton btnGetHistorical	= new VButton("Get Historical");
		
	@Override
	public VLayout getVisualComponent() throws VCodeException {
		
		initialize();
		
		// Main Structure
			layoutDetail		.addComponent(gridData);
			tabs				.addTab("Detail", layoutDetail);

			layoutTop			.addComponents(dProcessDate,btnCalculate,btnSave,btnGetHistorical);
			layoutContent		.addComponents(lblData,tabs);
			layoutMainV			.addComponents(lblTitle,layoutTop,layoutContent);
		
		return layoutMainV;
	}
	
private void initialize() {
	
	// Set Initial Values
		LocalDate ldNow = V3System.getSystemDate();
		dProcessDate.setValue(ldNow);
	
	// Layout Styles
		layoutMainV			.setStyle("vertical col-12");
		layoutContent		.setStyle("horizontal center");
		layoutTop			.setStyle("horizontal center bottom");
		
	// Components Styles
		dProcessDate		.setDateFormat("yyyy-MM-dd");
		tabs				.setWidth( "100%" );
		lblTitle			.setStyle( "center subtitle-XL" );
		lblData				.setStyle( "center subtitle-L" );
		btnCalculate		.setStyle( "tiny secondary red iconLeft fa-calculator marginMe-top");
		btnSave				.setStyle( "tiny secondary blue iconLeft fa-save marginMe-top v-disabled");
		btnGetHistorical	.setStyle( "tiny primary   blue iconLeft fa-search");
		
		PasivosPresenter presenter = new PasivosPresenter(this);
	}

	@Override
	public void setDisplayReference(IDisplayReference iDisplayReference) {
		display = iDisplayReference;
	}
}
