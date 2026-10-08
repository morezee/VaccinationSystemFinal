/* Connects the unchanged ImmuneCare screen to the Spring API. */
(() => {
  const endpoint = '/api/immune-care';
  const json = async (path, options = {}) => {
    const response = await fetch(endpoint + path, {
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
      ...options
    });
    const type = response.headers.get('content-type') || '';
    const result = type.includes('application/json') ? await response.json() : null;
    if (!response.ok) throw new Error(result?.message || `Request failed (${response.status}).`);
    if (!result) throw new Error('Your session has ended. Please sign in again.');
    return result;
  };

  const refresh = async () => {
    const data = await json('/bootstrap');
    S.patients = data.patients.map(p => ({ ...p, sex: (p.sex || 'Other').toLowerCase().replace(/^./, c => c.toUpperCase()) }));
    S.batches = data.batches;
    S.vax = data.vax;
    S.aefi = data.aefi;
    S.users = data.users;
    S.audit = data.audit;
    return data;
  };
  const error = message => { const el = $('#me') || $('#le'); if (el) el.textContent = message; else toast(message); };
  const save = async (path, body) => json(path, { method: 'POST', body: JSON.stringify(body) });

  window.doLogin = async event => {
    event.preventDefault();
    $('#le').textContent = '';
    const form = new URLSearchParams({ username: $('#lu').value.trim(), password: $('#lp').value });
    try {
      await fetch('/login', { method: 'POST', body: form, credentials: 'same-origin', headers: { 'Content-Type': 'application/x-www-form-urlencoded' } });
      const data = await refresh();
      const account = data.users.find(u => u.u === data.user);
      const role = data.role === 'ROLE_ADMINISTRATOR' ? 'Administrator' : data.role === 'ROLE_MANAGER' ? 'Manager' : 'Staff';
      S.user = { u: data.user, name: data.displayName || account?.name || data.user, role, on: true };
      S.view = 'dash'; S.sel = null;
      $('#login').hidden = true; $('#app').hidden = false; bump(); render();
    } catch (e) {
      $('#le').textContent = e.message.includes('session') ? 'Username or password is incorrect.' : e.message;
    }
  };

  window.reg = async () => {
    const val = i => $('#f' + i).value.trim();
    try {
      const created = await save('/patients', { name: val(1), nid: val(2), dob: val(3), sex: val(4), tel: val(5) });
      await refresh(); closeM(); toast('Patient registered: ' + created.id); S.sel = created.id; render();
    } catch (e) { error(e.message); }
  };

  window.vax = async id => {
    try {
      await save('/vaccinations', { pid: id, vac: $('#v1').value, dose: $('#v2').value, date: $('#v3').value, lot: $('#v4').value });
      await refresh(); closeM(); toast('Vaccination recorded'); render();
    } catch (e) { error(e.message); }
  };

  window.aefi = async vaccinationId => {
    const note = $('#a2').value.trim();
    if (!note) return;
    try {
      await save('/aefi', { vid: vaccinationId, severity: $('#a1').value, note });
      await refresh(); closeM(); toast('Adverse event logged'); render();
    } catch (e) { error(e.message); }
  };

  window.ship = async () => {
    const val = i => $('#s' + i).value.trim();
    try {
      await save('/shipments', { vac: val(1), lot: val(2), quantity: val(3), manufacturer: val(4), manufactureDate: val(5), expiryDate: val(6) });
      await refresh(); closeM(); toast('Shipment logged'); render();
    } catch (e) { error(e.message); }
  };

  window.userForm = () => modal(`<h2>Create user</h2><p class="err" id="me"></p><div class="row"><label>Full name<input id="u2"></label><label>Username<input id="u1" autocomplete="off"></label></div><div class="row"><label>Password<input id="u4" type="password" autocomplete="new-password" minlength="8"></label><label>Role<select id="u3"><option>Staff</option><option>Manager</option><option>Administrator</option></select></label></div><div class="row"><button class="btn" onclick="addU()">Create user</button><button class="btn ghost" onclick="closeM()">Cancel</button></div>`);
  window.addU = async () => {
    try {
      const created = await save('/users', { username: $('#u1').value.trim(), name: $('#u2').value.trim(), password: $('#u4').value, role: $('#u3').value });
      await refresh(); closeM(); toast('User created: ' + created.username); render();
    } catch (e) { error(e.message); }
  };
  window.tog = () => toast('Account activation is not available in the supplied database schema.');
})();
