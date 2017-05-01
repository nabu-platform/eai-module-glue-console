package be.nabu.eai.module.glue.console;

import java.util.List;

import javafx.scene.DepthTest;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import be.nabu.glue.annotations.GlueParam;
import be.nabu.glue.core.api.Lambda;
import be.nabu.glue.core.impl.GlueUtils;
import be.nabu.glue.core.impl.methods.v2.SeriesMethods;
import be.nabu.libs.evaluator.annotations.MethodProviderClass;

@MethodProviderClass(namespace = "chart")
public class ChartMethods {
	
	@SuppressWarnings("rawtypes")
	public static XYChart line(@GlueParam(name = "title") String title, @GlueParam(name = "x") String x, @GlueParam(name = "y") String y, @GlueParam(name = "plot") Object...objects) {
		List<?> resolved = SeriesMethods.resolve(GlueUtils.toSeries(objects));
		return Plotter.buildLineChart(x, y, title, resolved.toArray(new Series[0]));
	}
	
	@SuppressWarnings("rawtypes")
	public static XYChart scatter(@GlueParam(name = "title") String title, @GlueParam(name = "x") String x, @GlueParam(name = "y") String y, @GlueParam(name = "plot") Object...objects) {
		List<?> resolved = SeriesMethods.resolve(GlueUtils.toSeries(objects));
		return Plotter.buildScatterChart(x, y, title, resolved.toArray(new Series[0]));
	}
	
	@SuppressWarnings("rawtypes")
	public static XYChart area(@GlueParam(name = "title") String title, @GlueParam(name = "x") String x, @GlueParam(name = "y") String y, @GlueParam(name = "plot") Object...objects) {
		List<?> resolved = SeriesMethods.resolve(GlueUtils.toSeries(objects));
		return Plotter.buildAreaChart(x, y, title, resolved.toArray(new Series[0]));
	}
	
	@SuppressWarnings("rawtypes")
	public static XYChart bubble(@GlueParam(name = "title") String title, @GlueParam(name = "x") String x, @GlueParam(name = "y") String y, @GlueParam(name = "plot") Object...objects) {
		List<?> resolved = SeriesMethods.resolve(GlueUtils.toSeries(objects));
		return Plotter.buildBubbleChart(x, y, title, resolved.toArray(new Series[0]));
	}
	
	@SuppressWarnings("rawtypes")
	public static XYChart bar(@GlueParam(name = "title") String title, @GlueParam(name = "x") String x, @GlueParam(name = "y") String y, @GlueParam(name = "plot") Object...objects) {
		List<?> resolved = SeriesMethods.resolve(GlueUtils.toSeries(objects));
		return Plotter.buildBarChart(x, y, title, resolved.toArray(new Series[0]));
	}
	
	@SuppressWarnings("rawtypes")
	public static PieChart pie(@GlueParam(name = "title") String title, @GlueParam(name = "plot") Series plot) {
		return Plotter.buildPieChart(title, plot);
	}
	
	@SuppressWarnings("rawtypes")
	public static Series plot(@GlueParam(name = "name") String name, @GlueParam(name = "data") Object...objects) {
		List<?> resolved = SeriesMethods.resolve(GlueUtils.toSeries(objects));
		if (resolved.isEmpty()) {
			return null;
		}
		else if (resolved.get(0) instanceof Lambda) {
			int from = resolved.size() >= 3 ? GlueUtils.convert(resolved.get(2), Integer.class) : 0;
			int to = resolved.size() >= 2 ? GlueUtils.convert(resolved.get(1), Integer.class) : 1000;
			return Plotter.buildSeries(name, (Lambda) resolved.get(0), from, to);
		}
		else {
			return Plotter.buildSeries(name, resolved);
		}
	}
	
	// http://stackoverflow.com/questions/31073007/how-to-create-a-3d-surface-chart-with-javafx
	public static MeshView mesh(@GlueParam(name = "data") Object...objects) {
		List<?> resolved = SeriesMethods.resolve(GlueUtils.toSeries(objects));
		TriangleMesh mesh = Plotter.buildMesh(resolved);
		
		PhongMaterial material = new PhongMaterial();
//		material.setDiffuseMap(diffuseMap);
		material.setSpecularColor(Color.WHITE);

		MeshView meshView = new MeshView(mesh);
		meshView.setTranslateX(-0.5 * resolved.size());
		meshView.setTranslateZ(-0.5 * resolved.size());
		meshView.setMaterial(material);
		meshView.setCullFace(CullFace.NONE);
		meshView.setDrawMode(DrawMode.FILL);
		meshView.setDepthTest(DepthTest.ENABLE);
		return meshView;
	}
}
