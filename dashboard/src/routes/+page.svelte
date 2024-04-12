<script lang="ts">
	import FileInput from '$lib/FileInput.svelte';
	import Map from '$lib/Map.svelte';
	import { format, fromUnixTime, getMinutes } from 'date-fns';
	// Import other chart components
	import { personsList, selectedPerson } from '$lib/data.store';
	import dataExample from '$lib/dataExample.json';
	import dataExample2 from '$lib/dataExample2.json';

	import { onMount } from 'svelte';
	import Chart from '$lib/Chart.svelte';

	let labels = [];
	let fromTimeToTime = '';
	let duration = '';
	$: if ($selectedPerson && $selectedPerson.data) {
		// the the time of the first and last data point
		fromTimeToTime = `${format(fromUnixTime($selectedPerson.data[0].timestamp / 1000), 'HH:mm:ss')} - ${format(fromUnixTime($selectedPerson.data[$selectedPerson.data.length - 1].timestamp / 1000), 'HH:mm:ss')}`;
		// calculate the duration of the data in munites and seconds
		duration = `${getMinutes($selectedPerson.data[$selectedPerson.data.length - 1].timestamp - $selectedPerson.data[0].timestamp)} minutes`;

		labels = $selectedPerson.data.map(
			(d) => format(fromUnixTime(d.timestamp / 1000), 'HH:mm:ss')

			// new Date(d.timestamp * 100).toLocaleTimeString('nl-NL', {
			// 	hour: '2-digit',
			// 	minute: '2-digit',
			// 	hour12: false
			// })
		);
	}

	function selectPerson(id: number) {
		const person = $personsList.find((p) => p.id === id);
		if (person) {
			$selectedPerson = person;
		}
	}

	onMount(() => {
		personsList.update((d) => [...d, dataExample]);
		personsList.update((d) => [...d, dataExample2]);
		selectPerson(dataExample.id);
	});

	$: if ($selectedPerson) {
		// Refresh the charts by creating a function that recreates data objects for charts
		refreshCharts();
	}

	function refreshCharts() {
		// Logic to update chart data
		console.log('Data for charts refreshed for', $selectedPerson.id);
	}
</script>

<!-- <MapView /> -->

<div class="grid w-full gap-4 grid-cols-[auto_1fr] p-4 container mx-auto">
	<div class="">
		<h1 class="text-4xl font-bold mb-10">SmartPlay</h1>

		<!-- LIST OF DROPPED PERSONS -->
		{#each $personsList as person}
			<div>
				<button
					on:click={() => selectPerson(person.id)}
					class="btn w-full {person.id === $selectedPerson?.id ? 'btn-primary' : ''}"
					>{person.id} - {person.deviceId} ({person.data.length})<br />
					{new Date(person.timestamp).toLocaleString('en-US', {
						year: 'numeric',
						month: '2-digit',
						day: '2-digit',
						hour: '2-digit',
						minute: '2-digit'
					})}</button
				>
			</div>
		{/each}

		<!-- FILE INPUT -->
		<div class="p-10">
			<FileInput />
		</div>
	</div>

	<div class="">
		<!-- <pre>{JSON.stringify($selectedPerson, null, 2)}</pre> -->

		{#if $selectedPerson}
			<div class="mb-4">
				Number of data points: {$selectedPerson.data.length} - duration: {duration} ({fromTimeToTime})
			</div>

			<div class="grid grid-cols-1">
				<!-- All -->
				<Chart
					chartType="line"
					data={{
						labels,
						datasets: [
							{
								label: 'Heart Rate',
								backgroundColor: '#c1121f',
								borderColor: '#c1121f',
								data: $selectedPerson.data.map((d) => d.heartRate)
							},
							{
								label: 'Gyro X',
								backgroundColor: '#8ecae6',
								borderColor: '#8ecae6',
								data: $selectedPerson.data.map((d) => d.gyroX)
							},
							{
								label: 'Gyro Y',
								backgroundColor: '#0077b6',
								borderColor: '#0077b6',
								data: $selectedPerson.data.map((d) => d.gyroY),
								hidden: true
							},
							{
								label: 'Gyro Z',
								backgroundColor: '#457b9d',
								borderColor: '#457b9d',
								data: $selectedPerson.data.map((d) => d.gyroZ),
								hidden: true
							},
							{
								label: 'Accel X',
								backgroundColor: '#ffd6ff',
								borderColor: '#e7c6ff',
								data: $selectedPerson.data.map((d) => d.accelX)
							},
							{
								label: 'Accel Y',
								backgroundColor: '#e7c6ff',
								borderColor: '#e7c6ff',
								data: $selectedPerson.data.map((d) => d.accelY),
								hidden: true
							},
							{
								label: 'Accel Z',
								backgroundColor: '#a38cc6',
								borderColor: '#a38cc6',
								data: $selectedPerson.data.map((d) => d.accelZ),
								hidden: true
							},
							{
								label: 'Magneto X',
								backgroundColor: '#a3b18a',
								borderColor: '#a3b18a',
								data: $selectedPerson.data.map((d) => d.magnetoX)
							},
							{
								label: 'Magneto Y',
								backgroundColor: '#588157',
								borderColor: '#588157',
								data: $selectedPerson.data.map((d) => d.magnetoY),
								hidden: true
							},
							{
								label: 'Magneto Z',
								backgroundColor: '#3a5a40',
								borderColor: '#3a5a40',
								data: $selectedPerson.data.map((d) => d.magnetoZ),
								hidden: true
							}
						]
					}}
				/>
			</div>

			<div class="grid gap-10 grid-cols-2">
				<!-- Heart Rate -->
				<Chart
					chartType="line"
					data={{
						labels,
						datasets: [
							{
								label: 'Heart Rate',
								backgroundColor: 'rgba(255, 99, 132,0.4)',
								borderColor: 'rgba(255, 99, 132, 0.4)',
								data: $selectedPerson.data.map((d) => d.heartRate)
							}
						]
					}}
				/>
				<!-- Accelerometer -->
				<Chart
					chartType="radar"
					data={{
						labels,
						datasets: [
							{
								label: 'Accel X',
								backgroundColor: 'rgba(22, 99, 33,0.4)',
								borderColor: 'rgba(22, 99, 33,0.4)',
								data: $selectedPerson.data.map((d) => d.accelX)
							},
							{
								label: 'Accel Y',
								backgroundColor: 'rgba(22, 99, 132,0.4)',
								borderColor: 'rgba(22, 99, 132,0.4)',
								data: $selectedPerson.data.map((d) => d.accelY)
							},
							{
								label: 'Accel Z',
								backgroundColor: 'rgba(255, 99, 132,0.4)',
								borderColor: 'rgba(255, 99, 132, 0.4)',
								data: $selectedPerson.data.map((d) => d.accelZ)
							}
						]
					}}
				/>

				<!-- Gyroscope -->
				<Chart
					chartType="line"
					data={{
						labels,
						datasets: [
							{
								label: 'Gyro X',
								backgroundColor: 'blue',
								borderColor: 'blue',
								data: $selectedPerson.data.map((d) => d.gyroX)
							},
							{
								label: 'Gyro Y',
								backgroundColor: '#ff3',
								borderColor: '#ff3',
								data: $selectedPerson.data.map((d) => d.gyroY)
							},
							{
								label: 'Gyro Z',
								backgroundColor: 'rgba(255, 99, 132,0.4)',
								borderColor: 'rgba(255, 99, 132, 0.4)',
								data: $selectedPerson.data.map((d) => d.gyroZ)
							}
						]
					}}
				/>

				<!-- Magnetometer -->
				<Chart
					chartType="line"
					data={{
						labels,
						datasets: [
							{
								label: 'Magneto X',
								backgroundColor: 'blue',
								borderColor: 'blue',
								data: $selectedPerson.data.map((d) => d.magnetoX)
							},
							{
								label: 'Magneto Y',
								backgroundColor: '#ff3',
								borderColor: '#ff3',
								data: $selectedPerson.data.map((d) => d.magnetoY)
							},
							{
								label: 'Magneto Z',
								backgroundColor: 'rgba(255, 99, 132,0.4)',
								borderColor: 'rgba(255, 99, 132, 0.4)',
								data: $selectedPerson.data.map((d) => d.magnetoZ)
							}
						]
					}}
				/>
			</div>
			<!-- <MapView gpsData={$selectedPerson} /> -->
			<div class="h-[300px]">
				<Map />
			</div>
		{/if}
	</div>
</div>
