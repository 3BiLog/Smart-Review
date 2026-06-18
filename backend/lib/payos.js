import PayOSModule from "@payos/node";

const PayOS = PayOSModule.default ?? PayOSModule;

let payosClient;

export function getPayOS() {

console.log("=== PAYOS ENV ===");
console.log("CLIENT_ID =", process.env.PAYOS_CLIENT_ID);
console.log("API_KEY_EXISTS =", !!process.env.PAYOS_API_KEY);
console.log("CHECKSUM_EXISTS =", !!process.env.PAYOS_CHECKSUM_KEY);

  if (!payosClient) {
    payosClient = new PayOS(
      process.env.PAYOS_CLIENT_ID,
      process.env.PAYOS_API_KEY,
      process.env.PAYOS_CHECKSUM_KEY,
    );
  }
  return payosClient;
}

export function generateOrderCode() {
  // PayOS requires a unique positive integer order code
  const base = Date.now() % 2_000_000_000;
  const rand = Math.floor(Math.random() * 1000);
  return base + rand;
}
