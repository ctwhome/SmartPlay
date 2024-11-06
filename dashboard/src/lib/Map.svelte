<script>
	import { MapLibre } from 'svelte-maplibre';
	import GeoJSON from '$lib/GeoJSON.svelte';
	import LineLayer from '$lib/LineLayer.svelte';
	import { selectedData } from '$lib/data.store';

	let currentData;

	$: {
		if ($selectedData && $selectedData.length > 0) {
			currentData = {
				type: 'Feature',
				properties: {},
				geometry: {
					type: 'LineString',
					coordinates: $selectedData.map((point) => [point.longitude, point.latitude])
				}
			};
		} else {
			currentData = null;
		}
	}

	const fallbackData = {
		type: 'Feature',
		properties: {
			name: 'Fallback'
		},
		geometry: {
			type: 'Point',
			coordinates: [-68.137, 45.137]
		}
	};
</script>

<MapLibre
	style="https://basemaps.cartocdn.com/gl/positron-gl-style/style.json"
	class="relative w-full h-full"
	standardControls
	center={[-68.137, 45.137]}
	zoom={5}
	hash
>
	{#if currentData}
		<GeoJSON id="selected-path" data={currentData}>
			<LineLayer
				layout={{ 'line-cap': 'round', 'line-join': 'round' }}
				paint={{
					'line-width': 5,
					'line-dasharray': [5, 2],
					'line-color': '#008800',
					'line-opacity': 0.8
				}}
			/>
		</GeoJSON>
	{:else}
		<GeoJSON id="fallback" data={fallbackData}>
			<LineLayer
				layout={{ 'line-cap': 'round', 'line-join': 'round' }}
				paint={{
					'line-width': 5,
					'line-dasharray': [5, 2],
					'line-color': '#888888',
					'line-opacity': 0.8
				}}
			/>
		</GeoJSON>
	{/if}
</MapLibre>

<style>
	:global(.map) {
		height: 100% !important;
	}
</style>
