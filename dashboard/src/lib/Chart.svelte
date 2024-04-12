<script lang="ts">
	import { onDestroy, onMount } from 'svelte';
	import Chart from 'chart.js/auto';

	type Data = {
		labels: string[];
		datasets: {
			label: string;
			backgroundColor: string;
			borderColor: string;
			data: number[];
		}[];
	};
	type ChartType =
		| 'line'
		| 'bar'
		| 'radar'
		| 'doughnut'
		| 'polarArea'
		| 'bubble'
		| 'pie'
		| 'scatter';

	export let chartType: ChartType = 'line';
	export let data: Data;
	export let options: any = {
		scales: {
			y: {
				beginAtZero: true
			}
		}
	};

	let chartInstance: Chart | null = null;
	let canvas: HTMLCanvasElement;

	function createChart() {
		if (!canvas) {
			return;
		}
		if (chartInstance) {
			chartInstance.destroy();
		}

		const ctx = canvas.getContext('2d');
		chartInstance = new Chart(ctx, {
			type: chartType,
			data,
			options
		});
	}

	onMount(() => {
		console.log('🎹 mounted');
		createChart();
	});

	onDestroy(() => {
		console.log('🎹 destroy');
		if (chartInstance) {
			chartInstance.destroy();
		}
	});

	$: if (data) {
		console.log('🎹 chart type changed to', chartType);
		createChart();
	}
	// Reactively update the chart when chartType changes
	$: if (chartInstance) {
		console.log('🎹 chart type changed to', chartType);
		createChart();
	}
</script>

<div class="w-full aspect-video">
	<!-- radio button to change type -->
	<div class="flex space-x-4">
		<label>
			<input type="radio" bind:group={chartType} value="line" /> Line
		</label>
		<label>
			<input type="radio" bind:group={chartType} value="bar" /> Bar
		</label>
		<label>
			<input type="radio" bind:group={chartType} value="radar" /> Radar
		</label>
		<label>
			<input type="radio" bind:group={chartType} value="doughnut" /> Doughnut
		</label>
		<label>
			<input type="radio" bind:group={chartType} value="polarArea" /> Polar Area
		</label>
		<label>
			<input type="radio" bind:group={chartType} value="bubble" /> Bubble
		</label>
		<label>
			<input type="radio" bind:group={chartType} value="pie" /> Pie
		</label>
		<label>
			<input type="radio" bind:group={chartType} value="scatter" /> Scatter
		</label>
	</div>

	<canvas bind:this={canvas} class="w-full"></canvas>
</div>
