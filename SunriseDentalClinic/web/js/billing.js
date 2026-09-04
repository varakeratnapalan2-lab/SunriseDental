/**
 * SunriseDental — Billing
 */
async function calculateBill(appointmentNumber) {
  const num = String(appointmentNumber).trim().toUpperCase();
  if (isMockMode()) {
    const apt = await fetchAppointmentByNumber(num);
    const store = getMockStore();
    const fee = store.consultationFee || apt.consultationFee || 1500;
    const treatmentCost = apt.treatmentPrice || store.treatments.find((t) => t.name === apt.treatmentType)?.price || 0;
    const total = treatmentCost + fee;
    return {
      appointmentNumber: apt.appointmentNumber,
      patientName: apt.patientName,
      treatmentType: apt.treatmentType,
      treatmentCost,
      consultationFee: fee,
      totalAmount: total,
      billDate: new Date().toISOString().slice(0, 10)
    };
  }
  return apiCall('POST', '/api/bills/calculate', { appointmentNumber: num });
}

function renderBillReceipt(container, bill) {
  if (!container || !bill) return;
  container.classList.remove('hidden');
  
  // Handle Mock Mode fallback data
  const contact = bill.patientContact || bill.contactNumber || 'N/A';
  const address = bill.patientAddress || bill.address || 'N/A';
  const dentist = bill.dentistName || 'N/A';
  
  container.innerHTML = `
    <div class="bill-receipt" id="printArea" style="background:#fff; color:#0f172a; max-width:400px; margin:0 auto; padding:30px 24px; border-radius:12px; box-shadow:0 10px 25px rgba(0,0,0,0.5); font-family:'Plus Jakarta Sans', sans-serif;">
      
      <!-- Receipt Header -->
      <header style="text-align:center; margin-bottom:24px; border-bottom:2px dashed #cbd5e1; padding-bottom:20px;">
        <div style="font-size:2.5rem; margin-bottom:8px;">🌅</div>
        <h2 style="margin:0; font-size:1.4rem; color:#0f172a; font-weight:700;">SUNRISE DENTAL CLINIC</h2>
        <p style="margin:4px 0 0 0; font-size:0.85rem; color:#64748b; text-transform:uppercase; letter-spacing:1px;">Colombo, Sri Lanka</p>
        <p style="margin:8px 0 0 0; font-size:0.9rem; font-weight:600; color:#3b82f6; background:#eff6ff; display:inline-block; padding:4px 12px; border-radius:20px;">Patient Invoice</p>
      </header>
      
      <!-- Patient Details -->
      <div style="margin-bottom:20px; font-size:0.9rem;">
        <div style="display:flex; justify-content:space-between; margin-bottom:8px;">
          <span style="color:#64748b;">Receipt No:</span>
          <strong style="color:#0f172a;">#INV-${escapeHtml(bill.appointmentNumber)}</strong>
        </div>
        <div style="display:flex; justify-content:space-between; margin-bottom:8px;">
          <span style="color:#64748b;">Date:</span>
          <strong style="color:#0f172a;">${formatDate(bill.billDate)}</strong>
        </div>
        <div style="display:flex; justify-content:space-between; margin-bottom:8px;">
          <span style="color:#64748b;">Patient Name:</span>
          <strong style="color:#0f172a;">${escapeHtml(bill.patientName)}</strong>
        </div>
        <div style="display:flex; justify-content:space-between; margin-bottom:8px;">
          <span style="color:#64748b;">Contact:</span>
          <strong style="color:#0f172a;">${escapeHtml(contact)}</strong>
        </div>
        <div style="display:flex; justify-content:space-between; margin-bottom:8px;">
          <span style="color:#64748b;">Address:</span>
          <strong style="color:#0f172a;">${escapeHtml(address)}</strong>
        </div>
        <div style="display:flex; justify-content:space-between;">
          <span style="color:#64748b;">Dentist:</span>
          <strong style="color:#0f172a;">Dr. ${escapeHtml(dentist)}</strong>
        </div>
      </div>
      
      <!-- Treatment Details -->
      <div style="background:#f8fafc; padding:16px; border-radius:8px; margin-bottom:20px;">
        <p style="margin:0 0 12px 0; font-weight:700; color:#0f172a; border-bottom:1px solid #e2e8f0; padding-bottom:8px;">Treatment Summary</p>
        <div style="display:flex; justify-content:space-between; margin-bottom:8px; font-size:0.95rem;">
          <span style="color:#475569;">${escapeHtml(bill.treatmentType)}</span>
          <strong style="color:#0f172a;">LKR ${Number(bill.treatmentCost).toLocaleString()}</strong>
        </div>
        <div style="display:flex; justify-content:space-between; font-size:0.95rem;">
          <span style="color:#475569;">Consultation Fee</span>
          <strong style="color:#0f172a;">LKR ${Number(bill.consultationFee).toLocaleString()}</strong>
        </div>
      </div>
      
      <!-- Total -->
      <div style="display:flex; justify-content:space-between; align-items:center; border-top:2px solid #0f172a; border-bottom:2px solid #0f172a; padding:12px 0; margin-bottom:24px;">
        <span style="font-size:1.1rem; font-weight:700; color:#0f172a; text-transform:uppercase;">Total Due</span>
        <strong style="font-size:1.4rem; color:#10b981;">LKR ${Number(bill.totalAmount).toLocaleString()}</strong>
      </div>
      
      <!-- Footer -->
      <footer style="text-align:center;">
        <p style="margin:0 0 8px 0; font-size:0.85rem; color:#64748b; font-style:italic;">Thank you for trusting us with your smile!</p>
      </footer>
    </div>
    
    <div class="no-print" style="text-align:center; margin-top:24px;">
      <button type="button" class="btn btn-primary" onclick="window.print()" style="padding:10px 24px; font-weight:600; font-size:1rem; box-shadow:0 4px 14px rgba(99,102,241,0.4);"><span style="margin-right:8px;">🖨️</span> Print Invoice</button>
    </div>`;
}

function initBillingPage() {
  const form = document.getElementById('billingForm');
  const receipt = document.getElementById('billReceipt');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const num = form.appointmentNumber.value.trim();
    if (!num) {
      showToast('Enter appointment number', 'error');
      return;
    }
    showLoading(true);
    try {
      const bill = await calculateBill(num);
      renderBillReceipt(receipt, bill);
      showToast('Bill calculated successfully', 'success');
    } catch (err) {
      receipt?.classList.add('hidden');
      showToast(err.message || 'Could not calculate bill', 'error');
    } finally {
      showLoading(false);
    }
  });
}
