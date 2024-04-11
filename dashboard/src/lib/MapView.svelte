<script lang="ts">
	import { onMount, onDestroy } from 'svelte';
	import { Map } from 'maplibre-gl';
	import 'maplibre-gl/dist/maplibre-gl.css';

	export let gpsData; //: { latitude: number; longitude: number }[] = [];

	let map;
	let mapContainer;

	const apiKey = 'qjyUv6NFtgF6poQcVJlE';

	onMount(() => {
		// console.log('🎹 gps data', gpsData);

		const initialState = { lng: 139.753, lat: 35.6844, zoom: 14 };

		map = new Map({
			container: mapContainer,
			style: `https://api.maptiler.com/maps/streets-v2/style.json?key=${apiKey}`,
			center: [initialState.lng, initialState.lat],
			zoom: initialState.zoom
		});
	});

	onDestroy(() => {
		map.remove();
	});
</script>

<div class="h-full w-full">
	<a href="https://www.maptiler.com" class="watermark"
		><img src="https://api.maptiler.com/resources/logo.svg" alt="MapTiler logo" /></a
	>
	<div class="map" bind:this={mapContainer}></div>
</div>

<!-- <script lang="ts">
	import { onMount } from 'svelte';
	import { Map } from 'maplibre-gl';
	import 'maplibre-gl/dist/maplibre-gl.css';

	export let gpsData: { latitude: number; longitude: number }[] = [];

	let mapConfig = {
		style: {
			version: 8,
			sources: {
				'raster-tiles': {
					type: 'raster',
					tiles: ['https://tile.openstreetmap.org/{z}/{x}/{y}.png'],
					tileSize: 256,
					attribution: '© OpenStreetMap contributors'
				}
			},
			layers: [
				{
					id: 'simple-tiles',
					type: 'raster',
					source: 'raster-tiles',
					minzoom: 0,
					maxzoom: 19
				}
			]
		},
		center: [gpsData[0]?.longitude || 0, gpsData[0]?.latitude || 0],
		zoom: 13
	};
</script>

<Map {mapConfig} />

<style>
	:global(.maplibregl-canvas) {
		height: 400px;
	}
</style>

 -->

<style>
	.map-wrap {
		position: relative;
		width: 100%;
		height: calc(100vh - 77px); /* calculate height of the screen minus the heading */
	}

	.map {
		position: absolute;
		width: 100%;
		height: 100%;
	}

	.watermark {
		position: absolute;
		left: 10px;
		bottom: 10px;
		z-index: 999;
	}
</style>
