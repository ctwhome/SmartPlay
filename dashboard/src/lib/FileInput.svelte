<script lang="ts">
	import { onMount } from 'svelte';
	import { fade } from 'svelte/transition';
	import Papa from 'papaparse';

	import { data } from '$lib/data.store';

	let isDragging = false;

	function parseFileName(fileName: string) {
		const parts = fileName.split('_');
		if (parts.length === 3) {
			const id = parts[0];
			const deviceId = parts[1];
			const timestamp = parseInt(parts[2].split('.')[0], 10);
			return { id, deviceId, timestamp };
		} else {
			return null; // Invalid filename format
		}
	}

	function handleFileSelect(event: DragEvent) {
		event.preventDefault();
		isDragging = false;
		const files = event.dataTransfer.files;
		if (files.length > 0) {
			for (let i = 0; i < files.length; i++) {
				const file = files[i];
				const fileName = file.name;
				const parsedInfo = parseFileName(fileName);
				if (parsedInfo) {
					console.log('ID:', parsedInfo.id);
					console.log('Sensor ID:', parsedInfo.deviceId);
					console.log('Timestamp:', new Date(parsedInfo.timestamp));
				} else {
					console.log('Invalid filename format:', fileName);
				}

				Papa.parse(file, {
					header: true,
					complete: (result) => {
						console.log('Parsed data:', file.name, result);

						// add result.data to the store data array
						data.update((d) => [
							...d,
							{
								id: parsedInfo.id,
								deviceId: parsedInfo.deviceId,
								timestamp: parsedInfo.timestamp,
								data: result.data
							}
						]);
					}
				});
			}
		}
	}

	onMount(() => {
		parent.addEventListener('dragover', (event) => {
			event.preventDefault();
			isDragging = true;
		});

		parent.addEventListener('drop', handleFileSelect);
	});
</script>

<div class="flex gap-3">
	<svg
		xmlns="http://www.w3.org/2000/svg"
		viewBox="0 0 24 24"
		fill="none"
		stroke="currentColor"
		stroke-width="2"
		stroke-linecap="round"
		stroke-linejoin="round"
		class="w-6 h-6"
	>
		<path
			d="M21 15v4.586c0 .88-.716 1.586-1.586 1.586H4.586C3.716 21.172 3 20.466 3 19.586V15M12 4v14M7 9l5-5 5 5"
		/>
	</svg>
	<div>Drop your CSV file(s) here.</div>
</div>

{#if isDragging}
	<div
		transition:fade={{ duration: 200 }}
		class="fixed top-0 left-0 w-screen h-screen bg-black bg-opacity-50 z-10"
	>
		<div class="flex items-center justify-center h-screen">
			<div class="flex gap-3">
				<svg
					xmlns="http://www.w3.org/2000/svg"
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					stroke-width="2"
					stroke-linecap="round"
					stroke-linejoin="round"
					class="w-6 h-6"
				>
					<path
						d="M21 15v4.586c0 .88-.716 1.586-1.586 1.586H4.586C3.716 21.172 3 20.466 3 19.586V15M12 4v14M7 9l5-5 5 5"
					/>
				</svg>
				<div>Drop your CSV file(s) here.</div>
			</div>
		</div>
	</div>
{/if}
