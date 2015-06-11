package com.bdmap.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerDragListener;
import com.baidu.mapapi.map.ArcOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

public class MainActivity extends Activity implements OnClickListener {
	/**
	 * 相关按钮
	 */
	// 百度地图控件
	private MapView mMapView = null;
	// 百度地图对象
	private BaiduMap bdMap;
	// 普通地图
	private Button normalMapBtn;
	// 卫星地图
	private Button satelliteMapBtn;
	// 实时路况交通图
	private Button trafficMapBtn;
	// 热力图
	private Button headMapBtn;
	// 定位按钮
	private Button locateBtn;
	// 覆盖物按钮
	private Button overlayBtn;

	/**
	 * 相关设置
	 */
	// 定位模式 （普通-跟随-罗盘）
	private LocationMode currentMode;
	// 定位图标描述
	private BitmapDescriptor currentMarker = null;
	//
	private LocationClient locClient;
	// 记录是否第一次定位
	private boolean isFirstLoc = true;

	// 当前经纬度
	private double latitude, longitude;

	/**
	 * marker
	 */
	private Marker marker1;
	/**
	 * 标记显示第几个覆盖物 1->marker 2->polygon 3->text 4->GroundOverlay(地形图图层) 5->dot
	 * 6->circle 7->arc 8->polyline
	 */
	private int overlayIndex = 0;

	/**
	 * 初始化全局 bitmap 信息，不用时及时 recycle
	 * 
	 * 构建marker图标
	 */
	BitmapDescriptor bitmap = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_marka);
	// GroundOptions
	BitmapDescriptor bitmap2 = BitmapDescriptorFactory
			.fromResource(R.drawable.csdn_blog);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//
		setContentView(R.layout.activity_main);

		init();
	}

	/**
	 * 初始化方法
	 */
	private void init() {
		mMapView = (MapView) findViewById(R.id.bmapview);
		/**
		 * 描述地图将要发生的变化
		 */
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);// 设置地图缩放级别
		bdMap = mMapView.getMap();
		/**
		 * 改变地图状态
		 */
		bdMap.setMapStatus(msu);

		normalMapBtn = (Button) findViewById(R.id.normal_map_btn);
		satelliteMapBtn = (Button) findViewById(R.id.satellite_map_btn);
		trafficMapBtn = (Button) findViewById(R.id.traffic_map_btn);
		headMapBtn = (Button) findViewById(R.id.heat_map_btn);
		locateBtn = (Button) findViewById(R.id.locate_btn);
		overlayBtn = (Button) findViewById(R.id.overlay_btn);

		normalMapBtn.setOnClickListener(this);
		satelliteMapBtn.setOnClickListener(this);
		trafficMapBtn.setOnClickListener(this);
		headMapBtn.setOnClickListener(this);
		locateBtn.setOnClickListener(this);
		overlayBtn.setOnClickListener(this);

		//
		normalMapBtn.setEnabled(false);
		currentMode = LocationMode.NORMAL;
		locateBtn.setText("普通");

		// 设置是否允许定位图层
		bdMap.setMyLocationEnabled(true);
		locClient = new LocationClient(this);
		locClient.registerLocationListener(locListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll");// 设置坐标类型
		option.setAddrType("all");
		option.setScanSpan(1000);// 设置发起定位请求的间隔时间ms
		locClient.setLocOption(option);
		locClient.start();

		// 对marker覆盖物添加点击事件
		bdMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker arg0) {
				if (arg0 == marker1) {
					final LatLng latLng = arg0.getPosition();
					// 将经纬度转换成屏幕上的点
					// Point point =
					// bdMap.getProjection().toScreenLocation(latLng);
					Toast.makeText(MainActivity.this, latLng.toString(),
							Toast.LENGTH_SHORT).show();
				}
				return false;
			}
		});

		/**
		 * 地图点击事件
		 */
		bdMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public boolean onMapPoiClick(MapPoi arg0) {
				return false;
			}

			@Override
			public void onMapClick(LatLng latLng) {
				displayInfoWindow(latLng);
			}
		});

		/**
		 * 拖拽事件
		 */
		bdMap.setOnMarkerDragListener(new OnMarkerDragListener() {
			@Override
			public void onMarkerDragStart(Marker arg0) {

			}

			@Override
			public void onMarkerDragEnd(Marker arg0) {
				Toast.makeText(
						MainActivity.this,
						"拖拽结束，新位置：" + arg0.getPosition().latitude + ", "
								+ arg0.getPosition().longitude,
						Toast.LENGTH_LONG).show();
				reverseGeoCodeByPosition(arg0.getPosition());
			}

			@Override
			public void onMarkerDrag(Marker arg0) {

			}
		});
	}

	/**
	 * 反地理编码得到地址信息
	 */
	private void reverseGeoCodeByPosition(LatLng latLng) {
		// 创建地理编码检索实例
		GeoCoder geoCoder = GeoCoder.newInstance();
		//
		OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
			// 反地理编码查询结果回调函数
			@Override
			public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
				if (result == null
						|| result.error != SearchResult.ERRORNO.NO_ERROR) {
					// 没有检测到结果
					Toast.makeText(MainActivity.this, "抱歉，未能找到结果",
							Toast.LENGTH_LONG).show();
				}
				Toast.makeText(MainActivity.this, "位置：" + result.getAddress(),
						Toast.LENGTH_LONG).show();
			}

			// 地理编码查询结果回调函数
			@Override
			public void onGetGeoCodeResult(GeoCodeResult result) {
				if (result == null
						|| result.error != SearchResult.ERRORNO.NO_ERROR) {
					// 没有检测到结果
				}
			}
		};
		// 设置地理编码检索监听者
		geoCoder.setOnGetGeoCodeResultListener(listener);
		//
		geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
		// 释放地理编码检索实例
		// geoCoder.destroy();
	}

	/**
	 * 定位监听器
	 */
	BDLocationListener locListener = new BDLocationListener() {

		@Override
		public void onReceivePoi(BDLocation location) {

		}

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null || bdMap == null) {
				return;
			}
			// 构造定位数据
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())//
					.direction(100)// 方向
					.latitude(location.getLatitude())//
					.longitude(location.getLongitude())//
					.build();
			// 设置定位数据
			bdMap.setMyLocationData(locData);
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			// 第一次定位的时候，那地图中心店显示为定位到的位置
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				// MapStatusUpdate描述地图将要发生的变化
				// MapStatusUpdateFactory生成地图将要反生的变化
				MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(ll);
				bdMap.animateMapStatus(msu);
				// bdMap.setMyLocationEnabled(false);
				Toast.makeText(getApplicationContext(), "移动。。。。。。"+location.getAddrStr(),
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.normal_map_btn:
			bdMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
			normalMapBtn.setEnabled(false);
			satelliteMapBtn.setEnabled(true);
			break;
		case R.id.satellite_map_btn:
			bdMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
			satelliteMapBtn.setEnabled(false);
			normalMapBtn.setEnabled(true);
			break;
		case R.id.traffic_map_btn:
			if (!bdMap.isTrafficEnabled()) {
				bdMap.setTrafficEnabled(true);
				trafficMapBtn.setText("关闭实时路况");
			} else {
				bdMap.setTrafficEnabled(false);
				trafficMapBtn.setText("打开实时路况");
			}
			break;
		case R.id.heat_map_btn:
			if (!bdMap.isBaiduHeatMapEnabled()) {
				bdMap.setBaiduHeatMapEnabled(true);
				headMapBtn.setText("关闭热力图");
			} else {
				bdMap.setBaiduHeatMapEnabled(false);
				headMapBtn.setText("打开热力图");
			}
			break;
		case R.id.locate_btn:
			switch (currentMode) {
			case NORMAL:
				locateBtn.setText("跟随");
				currentMode = LocationMode.FOLLOWING;
				break;
			case FOLLOWING:
				locateBtn.setText("罗盘");
				currentMode = LocationMode.COMPASS;
				break;
			case COMPASS:
				locateBtn.setText("普通");
				currentMode = LocationMode.NORMAL;
				break;
			}
			/**
			 * MyLocationConfiguration定位图层的显示方式
			 */
			bdMap.setMyLocationConfigeration(new MyLocationConfiguration(
					currentMode, true, currentMarker));
			break;
		case R.id.overlay_btn:
			switch (overlayIndex) {
			case 0:
				overlayBtn.setText("显示多边形覆盖物");
				addMarkerOverlay();
				break;
			case 1:
				overlayBtn.setText("显示文字覆盖物");
				addPolygonOptions();
				break;
			case 2:
				overlayBtn.setText("显示地形图图层覆盖物");
				addTextOptions();
				break;
			case 3:
				overlayBtn.setText("显示折线覆盖物");
				addGroundOverlayOptions();
				break;
			case 4:
				overlayBtn.setText("显示圆点覆盖物");
				addPolylineOptions();
				break;
			case 5:
				overlayBtn.setText("显示圆（空心）覆盖物");
				addDotOptions();
				break;
			case 6:
				overlayBtn.setText("显示折线覆盖物");
				addCircleOptions();
				break;
			case 7:
				overlayBtn.setText("显示marker覆盖物");
				addArcOptions();
				break;
			}
			overlayIndex = (overlayIndex + 1) % 8;
			break;
		}
	}

	/**
	 * 添加标注覆盖物
	 */
	private void addMarkerOverlay() {
		bdMap.clear();
		// 定义marker坐标点
		LatLng point = new LatLng(latitude, longitude);

		// 构建markerOption，用于在地图上添加marker
		OverlayOptions options = new MarkerOptions()//
				.position(point)// 设置marker的位置
				.icon(bitmap)// 设置marker的图标
				.zIndex(9)// 設置marker的所在層級
				.draggable(true);// 设置手势拖拽
		// 在地图上添加marker，并显示
		marker1 = (Marker) bdMap.addOverlay(options);
	}

	/**
	 * 添加多边形覆盖物
	 */
	private void addPolygonOptions() {
		bdMap.clear();
		// 定义多边形的五个顶点
		LatLng pt1 = new LatLng(latitude + 0.02, longitude);
		LatLng pt2 = new LatLng(latitude, longitude - 0.03);
		LatLng pt3 = new LatLng(latitude - 0.02, longitude - 0.01);
		LatLng pt4 = new LatLng(latitude - 0.02, longitude + 0.01);
		LatLng pt5 = new LatLng(latitude, longitude + 0.03);
		List<LatLng> points = new ArrayList<LatLng>();
		points.add(pt1);
		points.add(pt2);
		points.add(pt3);
		points.add(pt4);
		points.add(pt5);
		//
		PolygonOptions polygonOptions = new PolygonOptions();
		polygonOptions.points(points);
		polygonOptions.fillColor(0xAAFFFF00);
		polygonOptions.stroke(new Stroke(2, 0xAA00FF00));
		Overlay polygon = bdMap.addOverlay(polygonOptions);
	}

	/**
	 * 添加文字覆盖物
	 */
	private void addTextOptions() {
		bdMap.clear();
		LatLng latLng = new LatLng(latitude, longitude);
		TextOptions textOptions = new TextOptions();
		textOptions.bgColor(0xAAFFFF00) // 設置文字覆蓋物背景顏色
				.fontSize(28) // 设置字体大小
				.fontColor(0xFFFF00FF)// 设置字体颜色
				.text("我在这里啊！！！！") // 文字内容
				.rotate(-30) // 设置文字的旋转角度
				.position(latLng);// 设置位置
		bdMap.addOverlay(textOptions);
	}

	/**
	 * 添加地形图图层
	 */
	private void addGroundOverlayOptions() {
		bdMap.clear();
		LatLng southwest = new LatLng(latitude - 0.01, longitude - 0.012);// 西南
		LatLng northeast = new LatLng(latitude + 0.01, longitude + 0.012);// 东北
		LatLngBounds bounds = new LatLngBounds.Builder().include(southwest)
				.include(northeast).build();// 得到一个地理范围对象

		GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions();
		groundOverlayOptions.image(bitmap2);// 显示的图片
		groundOverlayOptions.positionFromBounds(bounds);// 显示的位置
		groundOverlayOptions.transparency(0.7f);// 显示的透明度
		bdMap.addOverlay(groundOverlayOptions);
	}

	/**
	 * 添加折线覆盖物
	 */
	private void addPolylineOptions() {
		bdMap.clear();
		// 点
		LatLng pt1 = new LatLng(latitude + 0.01, longitude);
		LatLng pt2 = new LatLng(latitude, longitude - 0.01);
		LatLng pt3 = new LatLng(latitude - 0.01, longitude - 0.01);
		LatLng pt5 = new LatLng(latitude, longitude + 0.01);
		List<LatLng> points = new ArrayList<LatLng>();
		points.add(pt1);
		points.add(pt2);
		points.add(pt3);
		points.add(pt5);
		//
		PolylineOptions polylineOptions = new PolylineOptions();
		polylineOptions.points(points);
		polylineOptions.color(0xFF000000);
		polylineOptions.width(4);// 折线线宽
		bdMap.addOverlay(polylineOptions);
	}

	/**
	 * 添加圆点覆盖物
	 */
	private void addDotOptions() {
		bdMap.clear();
		DotOptions dotOptions = new DotOptions();
		dotOptions.center(new LatLng(latitude, longitude));// 设置圆心坐标
		dotOptions.color(0XFFfaa755);// 颜色
		dotOptions.radius(25);// 设置半径
		bdMap.addOverlay(dotOptions);
	}

	/**
	 * 添加圆（空心）覆盖物
	 */
	private void addCircleOptions() {
		bdMap.clear();
		CircleOptions circleOptions = new CircleOptions();
		circleOptions.center(new LatLng(latitude, longitude));// 设置圆心坐标
		circleOptions.fillColor(0XFFfaa755);// 圆的填充颜色
		circleOptions.radius(150);// 设置半径
		circleOptions.stroke(new Stroke(5, 0xAA00FF00));// 设置边框
		bdMap.addOverlay(circleOptions);
	}

	/**
	 * 添加弧线覆盖物
	 */
	private void addArcOptions() {
		bdMap.clear();
		LatLng pt1 = new LatLng(latitude, longitude - 0.01);
		LatLng pt2 = new LatLng(latitude - 0.01, longitude - 0.01);
		LatLng pt3 = new LatLng(latitude, longitude + 0.01);
		ArcOptions arcOptions = new ArcOptions();
		arcOptions.points(pt1, pt2, pt3);// 设置弧线的起点、中点、终点坐标
		arcOptions.width(5);// 线宽
		arcOptions.color(0xFF000000);
		bdMap.addOverlay(arcOptions);
	}

	/**
	 * 显示弹出窗口覆盖物
	 */
	private void displayInfoWindow(final LatLng latLng) {
		// 创建infowindow展示的view
		Button btn = new Button(getApplicationContext());
		btn.setBackgroundResource(R.drawable.popup);
		btn.setText("点我点我~");
		BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory
				.fromView(btn);
		// infowindow点击事件
		OnInfoWindowClickListener infoWindowClickListener = new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick() {
				/**
				 * 解析点击的具体地理位置
				 */
				reverseGeoCodeByPosition(latLng);
				// 隐藏InfoWindow
				bdMap.hideInfoWindow();
			}
		};
		// 创建infowindow
		InfoWindow infoWindow = new InfoWindow(bitmapDescriptor, latLng, -47,
				infoWindowClickListener);

		// 显示InfoWindow
		bdMap.showInfoWindow(infoWindow);
	}

	/**
	 * 地图很耗电，一下是管理器生命周期
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		locClient.stop();
		bdMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
		// 回收bitmip资源
		bitmap.recycle();
	}

}
