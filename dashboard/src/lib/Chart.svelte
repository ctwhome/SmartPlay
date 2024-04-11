<script lang="ts">
	import { onDestroy, onMount } from 'svelte';
	import Chart from 'chart.js/auto';

	export type Data = {
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
	export let data: Data = {
		labels: ['Timestamp1', 'Timestamp2', 'Timestamp3'], // Replace with actual timestamps
		datasets: [
			{
				label: 'Accel X',
				backgroundColor: 'rgb(255, 99, 132)',
				borderColor: 'rgb(255, 99, 132)',
				data: [-0.44771573, -0.38307226, -1.0366893] // Replace with actual accelX data points
			},
			{
				label: 'Accel Y',
				backgroundColor: '#ff3',
				borderColor: '#ff3',
				data: [-1.44771573, -1.38307226, -20366893] // Replace with actual accelX data points
			}
		]
	};
	export let options: any = {
		scales: {
			y: {
				beginAtZero: true
			}
		}
	};

	let chartInstance: Chart | null = null;
	let canvas: HTMLCanvasElement;

	// Reactive block to react on chartType changes
	$: {
		if (chartInstance) {
			chartInstance.destroy();
			createChart();
		}
	}

	function createChart() {
		const ctx = canvas.getContext('2d');
		chartInstance = new Chart(ctx, {
			type: chartType,
			data,
			options
		});
	}

	onMount(() => {
		createChart();
	});

	onDestroy(() => {
		if (chartInstance) {
			chartInstance.destroy();
		}
	});
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
