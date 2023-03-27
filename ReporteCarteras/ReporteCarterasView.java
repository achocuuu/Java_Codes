/**
 * ***********************************************************************
 * Autor   : Jaime Ignacio Valenzuela - VMetrix International Spa.
 * Version : v3.0
 * Fecha   : 14/03/2022.
 * Proyecto: Pensiones Profuturo - ReporteCarteras
*/

package com.vmetrix.v3.custom.proyector.ReporteCarteras;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import com.vmetrix.v3.custom.commons.util.V3System;
import com.vmetrix.v3.vcode.tab.V3Tab;
import com.vmetrix.v3.vcode.ui.input.V3Combobox;

import cl.vmetrix.core.dto.DTO;
import cl.vmetrix.vcube.vcode.display.IDisplayReference;
import cl.vmetrix.vcube.vcode.types.IVCodeWindow;
import cl.vmetrix.vcube.vcode.types.VCodeException;
import cl.vmetrix.vcube.vcode.types.windowCode.VLayout;
import cl.vmetrix.vcube.vcode.types.windowCode.VLayout.Orientation;
import cl.vmetrix.vcube.vcode.types.windowCode.components.VButton;
import cl.vmetrix.vcube.vcode.types.windowCode.components.VDatePicker;
import cl.vmetrix.vcube.vcode.types.windowCode.components.VGrid;
import cl.vmetrix.vcube.vcode.types.windowCode.components.VLabel;
import cl.vmetrix.vcube.vcode.types.windowCode.components.VRadioButton;
import cl.vmetrix.vcube.vcode.types.windowCode.components.VTextField;


public class ReporteCarterasView implements IVCodeWindow {	
	
	// Basic Variables
		private IDisplayReference display;
		String sReportName 			= "ReporteCarteras";

	// Grids
		VGrid gridReporte				= new VGrid(sReportName);
		VGrid gridPerGan		= new VGrid(sReportName+"PerdidaGanancia");
		VDatePicker dProcessDate	= new VDatePicker("Process Date");
		VTextField k_proy = new VTextField("Cantidad de Fechas de Proyeccion en Meses");
		
	// Main Layouts
		VLayout layoutMainV			= VLayout.getInstance( Orientation.NONE );
		VLayout layoutTop			= VLayout.getInstance( Orientation.HORIZONTAL );
		VLayout layoutContent		= VLayout.getInstance( Orientation.NONE );
		VLayout layoutReporte		= VLayout.getInstance( Orientation.VERTICAL );
		VLayout layoutPerGan		= VLayout.getInstance( Orientation.VERTICAL );
	    V3Tab tabs 					= new V3Tab("Tabs");
	    
	// Components
		VLabel lblTitle				= new VLabel("Reporte Carteras");
		VLabel lblData				= new VLabel("Data");
		VButton btnCalculate		= new VButton("Calculate");
		
	@Override
	public VLayout getVisualComponent() throws VCodeException {
		
		initialize();
		
		// Main Structure
			layoutReporte		.addComponent(gridReporte);
			layoutPerGan	.addComponent(gridPerGan);
			tabs				.addTab("Reporte Carteras", layoutReporte);
			tabs				.addTab("Perdida Ganancia", layoutPerGan);
			
			layoutTop			.addComponents(dProcessDate,k_proy,btnCalculate);
			layoutContent		.addComponents(lblData,tabs);
			layoutMainV			.addComponents(lblTitle,layoutTop,layoutContent);
		
		return layoutMainV;
	}
	
	private void initialize() {
	
		// Set Initial Values
			LocalDate ldNow = V3System.getSystemDate();
			dProcessDate		.setValue(ldNow);	
			k_proy.setValue("1");
		
		// Layout Styles
			layoutMainV			.setStyle("vertical col-12");
			layoutContent		.setStyle("horizontal center");
			layoutTop			.setStyle("horizontal center bottom");
			
		// Components Styles
			dProcessDate		.setDateFormat("yyyy-MM-dd");
			tabs				.setWidth("100%");
			lblTitle			.setStyle( "center subtitle-XL" );
			lblData				.setStyle( "center subtitle-L" );
			btnCalculate		.setStyle( "tiny secondary red iconLeft fa-calculator marginMe-top");
			
			ReporteCarterasPresenter presenter = new ReporteCarterasPresenter(this);
	}

	@Override
	public void setDisplayReference(IDisplayReference iDisplayReference) {
		display = iDisplayReference;
	}
	

}