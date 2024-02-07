<script lang="ts">
	import FileInput from '$lib/FileInput.svelte';
	import MapView from '$lib/MapView.svelte';
	// Import other chart components
	import { data, selectPerson, selectedPerson } from '$lib/data.store';

	import { derived } from 'svelte/store';

	// let gpsData = [];
	let heartRateData = [];
	let selectedData = [];

	// const selectedData = derived([data], ([$data]) => $data.find((d) => d.id === selected) || []);
	// Other data arrays

	function handleFileParsed(event) {
		const data = event.detail;
		// Populate gpsData, heartRateData, etc., based on the CSV structure
	}
	// $: {
	// 	selectedData = $data.filter((d) => d.id === selected)[0] || [];
	// }
	// $: console.log(selectedData);
</script>

<div class="grid h-screen w-screen grid-cols-[auto_1fr]">
	<div class="">
		<h1 class="text-4xl font-bold">SmartPlay</h1>
		<!-- for each person in $data -->
		<!-- <pre>{JSON.stringify($data, null, 2)}</pre> -->
		{#each $data as person}
			<div>
				<button
					on:click={() => selectPerson(person.id)}
					class="btn w-full {person.id === $selectedPerson?.id ? 'btn-primary' : ''}"
					>{person.id} - {person.deviceId} <br />
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
			<!-- <pre>{JSON.stringify($selectedPerson, null, 2)}</pre> -->
			<MapView gpsData={$selectedPerson} />
			<!-- Other chart components -->
		{/if}
		<!-- Other chart components -->
	</div>
</div>
<!-- <FileInput on:fileParsed={handleFileParsed} /> -->
<!-- <MapView {gpsData} /> -->
