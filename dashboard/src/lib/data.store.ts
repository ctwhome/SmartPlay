// using svelte store to store data in an array of objects
// and exporting it to be used in other components
import { writable, get } from 'svelte/store';

type Data = {
  timestamp: string;
  latitude: string;
  longitude: string;
  heartRate: string;
  accelX: string;
  accelY: string;
  accelZ: string;
  gyroX: string;
  gyroY: string;
  gyroZ: string;
  magnetoX: string;
  magnetoY: string;
  magnetoZ: string;
};
type Person = {
  id: number | null;
  deviceId: string | null;
  timestamp: string | null;
  data: Data[];
};

// List of persons
export const personsList = writable<Person[]>([]);
export const selectedPerson = writable<Person | null>(null);

// get gps data from a specific person
export function selectPerson(id: number) {

  const person = get(personsList).find((person) => person.id === id);
  if (person) {
    selectedPerson.set(person);
  }
}

// export const selectedPersonId = writable<number | null>(null);

// export function getPersonData(id: number) {
//   const person = data.find((person) => person.id === id);
//   if (person) {
//     selectedPersonId.set(person.id);
//   }
// }

