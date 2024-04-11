<script lang="ts">
	import FileInput from '$lib/FileInput.svelte';
	import Map from '$lib/Map.svelte';
	// Import other chart components
	import { personsList, selectPerson, selectedPerson } from '$lib/data.store';
	import dataExample from '$lib/dataExample.json';

	import { onMount } from 'svelte';
	import Chart from '$lib/Chart.svelte';

	function handleFileParsed(event) {
		const data = event.detail;
		// Populate gpsData, heartRateData, etc., based on the CSV structure
	}
	// }
	// $: console.log(selectedData);
	let labels = [];
	$: {
		if ($selectedPerson) {
			$selectedPerson.data &&
				(labels = $selectedPerson.data.map((d) => {
					return new Date(d.timestamp * 1000).toLocaleTimeString('en-US', {
						hour: '2-digit',
						minute: '2-digit',
						hour12: false
					});
				}));
		}
	}

	onMount(() => {
		// data example
		personsList.update((d) => [...d, dataExample]);
		selectPerson(dataExample.id);
	});
</script>

<!-- <MapView /> -->

<div class="grid w-full gap-4 grid-cols-[auto_1fr] p-4">
	<div class="">
		<h1 class="text-4xl font-bold">SmartPlay</h1>
		<!-- for each person in $data -->
		<!-- <pre>{JSON.stringify($data, null, 2)}</pre> -->
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
		<div class="p-10">
			<FileInput on:fileParsed={handleFileParsed} />
		</div>
	</div>

	<div class="">
		{#if $selectedPerson}
			Number of data points: {$selectedPerson.data.length}

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
								data: $selectedPerson.data.map((d) => d.gyroY)
							},
							{
								label: 'Gyro Z',
								backgroundColor: '#457b9d',
								borderColor: '#457b9d',
								data: $selectedPerson.data.map((d) => d.gyroZ)
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
								data: $selectedPerson.data.map((d) => d.accelY)
							},
							{
								label: 'Accel Z',
								backgroundColor: '#a38cc6',
								borderColor: '#a38cc6',
								data: $selectedPerson.data.map((d) => d.accelZ)
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
								data: $selectedPerson.data.map((d) => d.magnetoY)
							},
							{
								label: 'Magneto Z',
								backgroundColor: '#3a5a40',
								borderColor: '#3a5a40',
								data: $selectedPerson.data.map((d) => d.magnetoZ)
							}
						]
					}}
				/>
			</div>

			<div class="h-[550px]">
				<Map />
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
					chartType="line"
					data={{
						labels,
						datasets: [
							{
								label: 'Accel X',
								backgroundColor: 'blue',
								borderColor: 'blue',
								data: $selectedPerson.data.map((d) => d.accelX)
							},
							{
								label: 'Accel Y',
								backgroundColor: '#ff3',
								borderColor: '#ff3',
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
		{/if}
	</div>
</div>
