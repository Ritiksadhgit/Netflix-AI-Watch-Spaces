// Timed WebVTT subtitle tracks for catalog titles with zero-drift switching
// Generates data: URLs with UTF-8 WebVTT text to avoid cross-origin CORS blocks on HTML5 video elements

const toVttDataUrl = (vttContent) => {
  return `data:text/vtt;charset=utf-8,${encodeURIComponent(vttContent.trim())}`;
};

// Tears of Steel (Title 1) - Sci-Fi / Cyberpunk
const TEARS_OF_STEEL_EN = `WEBVTT - Tears of Steel (English CC)

00:00:02.000 --> 00:00:08.000
[Wind howling over the desolate ruins of Amsterdam]

00:00:10.000 --> 00:00:16.000
Thom: The perimeter sensors are picking up drone signatures near the Oude Kerk canal.

00:00:17.000 --> 00:00:23.000
Celia: Keep the neural dampeners online. We cannot afford another uplink breach.

00:00:25.000 --> 00:00:32.000
Thom: Initiating quantum resonance calibration. Stand by for defensive shield sync.

00:00:35.000 --> 00:00:44.000
[Robotic servos whirr as Thom tests his cybernetic prosthetic arm]

00:00:48.000 --> 00:00:56.000
Thom: It feels responsive. Let's see if the relay holds when the swarm descends.

00:01:05.000 --> 00:01:14.000
Celia: Scout drone incoming from sector four! Weapons free!

00:01:25.000 --> 00:01:34.000
[Heavy ordnance firing echoes across the steel bridge]

00:01:45.000 --> 00:01:54.000
Thom: Hold your positions! Target the central processor node!

00:02:15.000 --> 00:02:25.000
Celia: Direct hit on their lead fighter! Swarm dispersion confirmed.

00:03:55.000 --> 00:04:02.000
[Tactical radar alarm chimes urgently]

00:04:03.000 --> 00:04:12.000
Tactical AI: Narrative decision point imminent. Multi-vector threat approaching.

00:04:13.000 --> 00:04:22.000
Thom: Watch party consensus requested! Vote on tactical countermeasure now!
`;

const TEARS_OF_STEEL_ES = `WEBVTT - Tears of Steel (Español)

00:00:02.000 --> 00:00:08.000
[El viento aúlla sobre las ruinas desoladas de Ámsterdam]

00:00:10.000 --> 00:00:16.000
Thom: Los sensores perimétricos detectan firmas de drones cerca del canal Oude Kerk.

00:00:17.000 --> 00:00:23.000
Celia: Mantén los amortiguadores neuronales activos. No podemos permitir otra brecha.

00:00:25.000 --> 00:00:32.000
Thom: Iniciando calibración de resonancia cuántica. En espera para la sincronización del escudo.

00:00:35.000 --> 00:00:44.000
[Los servomotores robóticos zumban mientras Thom prueba su prótesis cibernética]

00:00:48.000 --> 00:00:56.000
Thom: Responde bien. Veamos si el relé resiste cuando descienda el enjambre.

00:01:05.000 --> 00:01:14.000
Celia: ¡Dron de reconocimiento entrante desde el sector cuatro! ¡Fuego a discreción!

00:01:25.000 --> 00:01:34.000
[El fuego de artillería pesada resuena en el puente de acero]

00:01:45.000 --> 00:01:54.000
Thom: ¡Mantengan sus posiciones! ¡Apunten al nodo del procesador central!

00:02:15.000 --> 00:02:25.000
Celia: ¡Impacto directo en el caza líder! Dispersión del enjambre confirmada.

00:03:55.000 --> 00:04:02.000
[La alarma del radar táctico suena con urgencia]

00:04:03.000 --> 00:04:12.000
IA Táctica: Punto de decisión narrativa inminente. Amenaza multivectorial en camino.

00:04:13.000 --> 00:04:22.000
Thom: ¡Consenso de la sala requerido! ¡Voten la contramedida táctica ahora!
`;

const TEARS_OF_STEEL_FR = `WEBVTT - Tears of Steel (Français)

00:00:02.000 --> 00:00:08.000
[Le vent hurle sur les ruines désolées d'Amsterdam]

00:00:10.000 --> 00:00:16.000
Thom: Les capteurs de périmètre détectent des signatures de drones près du canal Oude Kerk.

00:00:17.000 --> 00:00:23.000
Celia: Gardez les amortisseurs neuronaux actifs. Nous ne pouvons risquer une autre intrusion.

00:00:25.000 --> 00:00:32.000
Thom: Initialisation de la calibration quantique. Préparez-vous à la synchro du bouclier.

00:00:35.000 --> 00:00:44.000
[Les servomoteurs robotiques vrombissent alors que Thom teste son bras cybernétique]

00:00:48.000 --> 00:00:56.000
Thom: C'est très réactif. Voyons si le relais tient le coup face à l'essaim.

00:01:05.000 --> 00:01:14.000
Celia: Drone éclaireur en approche depuis le secteur quatre ! Feu à volonté !

00:01:25.000 --> 00:01:34.000
[Des tirs d'artillerie lourde résonnent sur le pont d'acier]

00:01:45.000 --> 00:01:54.000
Thom: Maintenez vos positions ! Visez le processeur central !

00:02:15.000 --> 00:02:25.000
Celia: Coup au but sur leur chasseur de tête ! Dispersion de l'essaim confirmée.

00:03:55.000 --> 00:04:02.000
[L'alarme du radar tactique retentit d'urgence]

00:04:03.000 --> 00:04:12.000
IA Tactique: Point de décision narrative imminent. Menace multi-vecteur en approche.

00:04:13.000 --> 00:04:22.000
Thom: Vote du groupe requis ! Choisissez votre contre-mesure tactique maintenant !
`;

// Sintel (Title 2) - Fantasy / Adventure
const SINTEL_EN = `WEBVTT - Sintel (English CC)

00:00:03.000 --> 00:00:09.000
[Howling winter wind sweeping over snowy mountain crags]

00:00:12.000 --> 00:00:18.000
Narrator: She walked through the merciless cold, guided only by memory.

00:00:25.000 --> 00:00:33.000
Sintel: Scales... wings... where did you take him?

00:00:40.000 --> 00:00:48.000
[Soft fluttering of baby dragon wings in flashback memory]

00:01:15.000 --> 00:01:23.000
Sintel: I promised I would find you. No matter how far the mountain peaks reach.
`;

const SINTEL_ES = `WEBVTT - Sintel (Español)

00:00:03.000 --> 00:00:09.000
[Viento invernal aullando sobre los riscos montañosos nevados]

00:00:12.000 --> 00:00:18.000
Narrador: Caminó a través del frío implacable, guiada solo por sus recuerdos.

00:00:25.000 --> 00:00:33.000
Sintel: Escamas... alas... ¿adónde te llevaron?

00:00:40.000 --> 00:00:48.000
[Suave aleteo de un dragón bebé en el recuerdo]

00:01:15.000 --> 00:01:23.000
Sintel: Te prometí que te encontraría. Sin importar cuán lejos lleguen las cumbres.
`;

// Big Buck Bunny (Title 3) - Animation / Comedy
const BUNNY_EN = `WEBVTT - Big Buck Bunny (English CC)

00:00:05.000 --> 00:00:12.000
[Cheerful morning birds chirping in the serene forest glade]

00:00:15.000 --> 00:00:22.000
[Bunny yawns, stretches leisurely, and greets a gentle butterfly]

00:00:35.000 --> 00:00:42.000
[Rude rustling in the bushes as the woodland bullies scheme an ambush]

00:01:00.000 --> 00:01:10.000
[Bunny decides enough is enough: time to build ingenious forest traps!]
`;

/**
 * Returns available subtitle tracks for a given title ID.
 * @param {number|string} titleId
 * @returns {Array<{ id: string, label: string, lang: string, src: string, isDefault: boolean }>}
 */
export function getSubtitleTracksForTitle(titleId) {
  const idNum = Number(titleId);

  if (idNum === 2) {
    return [
      { id: 'en', label: 'English [CC]', lang: 'en', src: toVttDataUrl(SINTEL_EN), isDefault: true },
      { id: 'es', label: 'Español', lang: 'es', src: toVttDataUrl(SINTEL_ES), isDefault: false },
    ];
  }

  if (idNum === 3) {
    return [
      { id: 'en', label: 'English [CC]', lang: 'en', src: toVttDataUrl(BUNNY_EN), isDefault: true },
    ];
  }

  // Default / Title 1 (Tears of Steel)
  return [
    { id: 'en', label: 'English [CC]', lang: 'en', src: toVttDataUrl(TEARS_OF_STEEL_EN), isDefault: true },
    { id: 'es', label: 'Español', lang: 'es', src: toVttDataUrl(TEARS_OF_STEEL_ES), isDefault: false },
    { id: 'fr', label: 'Français', lang: 'fr', src: toVttDataUrl(TEARS_OF_STEEL_FR), isDefault: false },
  ];
}
